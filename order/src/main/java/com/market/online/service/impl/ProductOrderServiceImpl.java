package com.market.online.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.market.online.config.RabbitMQConfig;
import com.market.online.constant.CacheKey;
import com.market.online.dto.ConfirmOrderDto;
import com.market.online.enums.BizCodeEnum;
import com.market.online.enums.CouponStateEnum;
import com.market.online.enums.ProductOrderStateEnum;
import com.market.online.exceptions.BizException;
import com.market.online.feign.CouponFeignService;
import com.market.online.feign.ProductFeignService;
import com.market.online.feign.UserFeignService;
import com.market.online.interceptor.LoginInterceptor;
import com.market.online.model.OrderMessage;
import com.market.online.model.PayInfoDTO;
import com.market.online.model.ProductOrderDO;
import com.market.online.mapper.ProductOrderMapper;
import com.market.online.model.ProductOrderItemDO;
import com.market.online.pay.PayFactory;
import com.market.online.request.LockCouponRecordRequest;
import com.market.online.request.LockProductRequest;
import com.market.online.request.OrderItemRequest;
import com.market.online.service.ProductOrderItemService;
import com.market.online.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.market.online.util.CommonUtil;
import com.market.online.util.JsonData;
import com.market.online.vo.CartItemVO;
import com.market.online.vo.CouponRecordVO;
import com.market.online.vo.LoginUser;
import com.market.online.vo.OrderAddrVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LZX
 * @since 2025-02-19
 */
@Service
@Slf4j
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

    @Autowired
    private UserFeignService userFeignService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private ProductOrderItemService productOrderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private PayFactory payFactory;
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * @description:
     * @author: LZX
     * @date: 2025/2/20 15:28
     * @param: [confirmOrderDto]
     * @return: com.market.online.util.JsonData
     **/
    @Override
    public JsonData confirmOrder(ConfirmOrderDto confirmOrderDto) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String orderToken = confirmOrderDto.getToken();
        if(StringUtils.isBlank(orderToken)){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_NOT_EXIST);
        }
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] " +
                "then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(String.format(CacheKey.SUBMIT_ORDER_TOKEN_KEY,loginUser.getId())),orderToken);
        if(result == 0L){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_EQUAL_FAIL);
        }
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);
        OrderAddrVo orderAddrVo = this.getUserAddr(confirmOrderDto.getAddressId());
        log.info("收获地址为:{}", orderAddrVo);
        List<Long> productIdList = confirmOrderDto.getProductIdList();
        List<CartItemVO> voList = getProduct(productIdList);
        log.info("最新购物项和价格：{}", JSON.toJSONString(voList));
        this.calculatePrice(confirmOrderDto, voList);
        this.saveProductOrder(confirmOrderDto, loginUser, orderOutTradeNo, orderAddrVo);
        this.saveProductOrderItems(orderOutTradeNo, loginUser.getId(), voList);
        try {
            this.lockCouponRecords(confirmOrderDto.getCouponRecordId(), orderOutTradeNo);
            this.lockProductStocks(voList, orderOutTradeNo);
        } catch (Exception e) {
            cancelOrder(orderOutTradeNo);
            if (confirmOrderDto.getCouponRecordId() != null) {
                couponFeignService.unlockCouponRecord(confirmOrderDto.getCouponRecordId());
            }
            throw e;
        }
        this.sendDelayMessage(orderOutTradeNo);
        try {
            productFeignService.clearCartByProductIds(productIdList);
        } catch (Exception e) {
            log.error("清空购物车失败: {}", e.getMessage());
        }
        log.info("订单创建成功，不发送MQ延迟关闭消息（调试模式）");
        return JsonData.buildSuccess(orderOutTradeNo);
    }

    private void cancelOrder(String outTradeNo) {
        try {
            LambdaUpdateWrapper<ProductOrderDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
            wrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.CANCEL.name());
            update(wrapper);
        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage());
        }
    }

    private void sendDelayMessage(String orderOutTradeNo) {
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOutTradeNo(orderOutTradeNo);
        rabbitTemplate.convertAndSend(
            rabbitMQConfig.getEventExchange(),
            rabbitMQConfig.getOrderCloseDelayRoutingKey(),
            orderMessage,
            new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) {
                    message.getMessageProperties().setExpiration(String.valueOf(rabbitMQConfig.getTtl()));
                    return message;
                }
            }
        );
    }

    private void saveProductOrderItems(String orderOutTradeNo, Long id, List<CartItemVO> voList) {
        //获取购物车列表商品
        List<ProductOrderItemDO> collect = voList.stream().map(obj -> {
            ProductOrderItemDO productOrderItemDO = new ProductOrderItemDO();
            productOrderItemDO.setProductId(obj.getProductId());
            productOrderItemDO.setProductName(obj.getProductTitle());
            productOrderItemDO.setProductImg(obj.getProductImage());
            productOrderItemDO.setBuyNum(obj.getBuyNum());
            productOrderItemDO.setOutTradeNo(orderOutTradeNo);
            productOrderItemDO.setProductOrderId(id);
            productOrderItemDO.setTotalAmount(obj.getTotalAmount());
            productOrderItemDO.setAmount(obj.getProductPrice());
            return productOrderItemDO;
        }).collect(Collectors.toList());
        //批量保存订单项
        productOrderItemService.saveBatch(collect);

    }

    private ProductOrderDO saveProductOrder(ConfirmOrderDto confirmOrderDto, LoginUser loginUser, String orderOutTradeNo,OrderAddrVo orderAddrVo) {
        //创建对象设置属性
        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        productOrderDO.setPayType(confirmOrderDto.getPayType());
        productOrderDO.setTotalAmount(confirmOrderDto.getTotalAmount());
        productOrderDO.setPayAmount(confirmOrderDto.getRealPayAmount());
        productOrderDO.setOrderType("DAILY");
        productOrderDO.setUserId(loginUser.getId());
        productOrderDO.setReceiverAddress(JSON.toJSONString(orderAddrVo));
        productOrderDO.setNickname(loginUser.getName());
        productOrderDO.setHeadImg(loginUser.getHeadImg());
        //储存数据库
        save(productOrderDO);
        return productOrderDO;

    }

    private void lockProductStocks(List<CartItemVO> voList, String orderOutTradeNo) {
        //获取所需锁定的商品列表
        List<OrderItemRequest> collect = voList.stream().map(obj -> {
            OrderItemRequest request = new OrderItemRequest();
            request.setProductId(obj.getProductId());
            request.setBuyNum(obj.getBuyNum());
            return request;
        }).collect(Collectors.toList());
        //构建对象
        LockProductRequest lockProductRequest = new LockProductRequest();
        lockProductRequest.setOrderOutTradeNo(orderOutTradeNo);
        lockProductRequest.setOrderItemRequest(collect);
        //远程调用锁定商品
        JsonData jsonData = productFeignService.lockProduct(lockProductRequest);
        if (jsonData.getCode() != 0) {
            log.error("[商品微服务]-锁定库存失败");
            throw new BizException(BizCodeEnum.STOCK_LOCK_FAIL);
        }

    }

    private void lockCouponRecords(Long couponRecordId, String orderOutTradeNo) {
        //检测优惠券id是否有效
        if (couponRecordId == null || couponRecordId < 0) {
            return;
        }
        //远程调用锁定优惠券接口
        LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
        lockCouponRecordRequest.setOutTradeNo(orderOutTradeNo);
        List<Long> list = new ArrayList<>();
        list.add(couponRecordId);
        lockCouponRecordRequest.setCouponRecordIds(list);
        JsonData jsonData = couponFeignService.lockRecords(lockCouponRecordRequest);
        //检查是否成功锁定优惠券
        if (jsonData.getCode() != 0) {
            log.error("[优惠券微服务]-锁定优惠卷失败");
            throw new BizException(BizCodeEnum.COUPON_LOCK_FAIL);
        }
    }

    private BigDecimal calculatePrice(ConfirmOrderDto confirmOrderDto, List<CartItemVO> voList) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (voList != null) {
            for (CartItemVO cartItemVO : voList) {
                totalPrice = totalPrice.add(cartItemVO.getTotalAmount());
            }
        }
        log.info("原始总价 - 后端: {}, 前端 totalAmount: {}", totalPrice, confirmOrderDto.getTotalAmount());
        if (totalPrice.compareTo(confirmOrderDto.getTotalAmount()) != 0) {
            log.warn("总价不一致，以后端为准: backend={}, frontend={}", totalPrice, confirmOrderDto.getTotalAmount());
        }
        confirmOrderDto.setTotalAmount(totalPrice);

        CouponRecordVO couponRecordVO = this.getCartCouponRecord(confirmOrderDto.getCouponRecordId());
        if (couponRecordVO != null) {
            log.info("使用优惠券: id={}, price={}, conditionPrice={}",
                    confirmOrderDto.getCouponRecordId(), couponRecordVO.getPrice(), couponRecordVO.getConditionPrice());
            if (totalPrice.compareTo(couponRecordVO.getConditionPrice()) < 0) {
                log.error("不符合满减规则: totalPrice={}, conditionPrice={}", totalPrice, couponRecordVO.getConditionPrice());
                throw new BizException(BizCodeEnum.COUPON_FAIL);
            }
            if (couponRecordVO.getPrice().compareTo(totalPrice) > 0) {
                totalPrice = BigDecimal.ZERO;
            } else {
                totalPrice = totalPrice.subtract(couponRecordVO.getPrice());
            }
        }
        log.info("最终支付价 - 后端: {}, 前端 realPayAmount: {}", totalPrice, confirmOrderDto.getRealPayAmount());
        if (totalPrice.compareTo(confirmOrderDto.getRealPayAmount()) != 0) {
            log.warn("支付价不一致，以后端为准: backend={}, frontend={}", totalPrice, confirmOrderDto.getRealPayAmount());
        }
        confirmOrderDto.setRealPayAmount(totalPrice);
        return totalPrice;
    }

    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {
        if (couponRecordId == null || couponRecordId < 0) {
            return null;
        }
        JsonData recordDetail = couponFeignService.detailCouponRecord(couponRecordId);
        if (recordDetail.getCode() != 0) {
            log.error("[优惠券微服务]-获取优惠券信息失败");
            throw new BizException(BizCodeEnum.COUPON_STATE);
        }
        CouponRecordVO data = recordDetail.getData(new TypeReference<CouponRecordVO>() {
        });
        //检查是否在有效期之内
        long currentTimeMillis = System.currentTimeMillis();
        long end = data.getEndTime().getTime();
        long start = data.getStartTime().getTime();
        if (currentTimeMillis < start || currentTimeMillis > end) {
            log.error("[优惠券微服务]-优惠券状态异常");
            throw new BizException(BizCodeEnum.COUPON_NOT_FAIL);
        }
        //是否为已使用
        if (data.getUseState().equalsIgnoreCase(CouponStateEnum.USED.name())) {
            log.error("优惠券已使用过");
            throw new BizException(BizCodeEnum.COUPON_LOCK_FAIL);
        }
        return data;
    }

    private List<CartItemVO> getProduct(List<Long> productIdList) {
        JsonData jsonData = productFeignService.confirmOrderCartItems(productIdList);
        if (jsonData.getCode() != 0) {
            log.error("[商品微服务]-获取最新购物项和价格失败");
            throw new BizException(BizCodeEnum.PRODUCT_PRICE_FAIL);
        }
        List<CartItemVO> data = jsonData.getData(new TypeReference<List<CartItemVO>>() {});
        return data;
    }

    @Override
    public JsonData queryProductOrderState(String outTradeNo) {
        LambdaQueryWrapper<ProductOrderDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
        ProductOrderDO one = getOne(lambdaQueryWrapper);
        return one == null?JsonData.buildError("订单不存在"):JsonData.buildSuccess(one);
    }

    @Override
    public OrderAddrVo getUserAddr(Long addressId) {

        JsonData jsonData = userFeignService.find(addressId);
        if (jsonData.getCode() != 0) {
            log.error("确认收货地址失败");
            throw new BizException(BizCodeEnum.USER_ADDR_FAIL);
        }
        OrderAddrVo data = jsonData.getData(new TypeReference<OrderAddrVo>() {
        });
        return data;
    }

    @Override
    public boolean closeProductOrder(String outTradeNo) {
        LambdaQueryWrapper<ProductOrderDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
        ProductOrderDO one = getOne(lambdaQueryWrapper);
        if (one == null) {
            log.error("订单不存在");
            return true;
        }
        if (one.getState().equalsIgnoreCase(ProductOrderStateEnum.PAY.name())) {
            log.info("直接确认消息,订单已经支付:{}");
            return true;
        }
        if (!"NEW".equalsIgnoreCase(one.getState())) {
            return true;
        }
        try {
            PayInfoDTO payInfoDTO = new PayInfoDTO();
            payInfoDTO.setPayType(one.getPayType());
            payInfoDTO.setOutTradeNo(one.getOutTradeNo());
            String result = payFactory.queryPaySuccess(payInfoDTO);
            if (StringUtils.isNotBlank(result) && "TRADE_SUCCESS".equalsIgnoreCase(result)) {
                LambdaUpdateWrapper<ProductOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
                lambdaUpdateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.PAY.name());
                update(lambdaUpdateWrapper);
                return true;
            }
        } catch (Exception e) {
            log.warn("查询支付宝支付状态失败，30分钟超时取消订单: {}", e.getMessage());
        }
        log.info("30分钟超时，取消订单");
        LambdaUpdateWrapper<ProductOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
        lambdaUpdateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.CANCEL.name());
        update(lambdaUpdateWrapper);
        couponFeignService.unlockByOutTradeNo(outTradeNo);
        return true;
    }
    @Override
    public void cancelOrderAndUnlockCoupon(String outTradeNo, Long couponRecordId) {
        cancelOrder(outTradeNo);
        if (couponRecordId != null) {
            couponFeignService.unlockCouponRecord(couponRecordId);
        }
        couponFeignService.unlockByOutTradeNo(outTradeNo);
    }

    public boolean handlerOrderCallbackMsg(Map<String, String> paramsMap) {
        String out_trade_no = paramsMap.get("out_trade_no");
        String trade_status = paramsMap.get("trade_status");
        if (trade_status.equals("TRADE_SUCCESS")) {
            LambdaUpdateWrapper<ProductOrderDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(ProductOrderDO::getOutTradeNo, out_trade_no);
            lambdaUpdateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.PAY.name());
            update(lambdaUpdateWrapper);
            try {
                couponFeignService.confirmCouponByOutTradeNo(out_trade_no);
            } catch (Exception e) {
                log.error("确认优惠券任务失败: {}", e.getMessage());
            }
            return true;
        }
        return false;
    }
}
