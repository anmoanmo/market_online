package com.buka.controller;


import com.alibaba.fastjson.JSON;
import com.buka.constant.CacheKey;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.dto.ConfirmOrderDto;
import com.buka.enums.BizCodeEnum;
import com.buka.enums.ClientType;
import com.buka.enums.ProductOrderPayTypeEnum;
import com.buka.enums.ProductOrderStateEnum;
import com.buka.exceptions.BizException;
import com.buka.model.ProductOrderDO;
import com.buka.model.ProductOrderItemDO;
import com.buka.service.ProductOrderItemService;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.PayInfoDTO;
import com.buka.pay.PayFactory;
import com.buka.service.ProductOrderService;
import com.buka.util.AuthUtil;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LZX
 * @since 2025-02-19
 */
@Slf4j
@RestController
@RequestMapping("/api/productOrder/v1")
public class ProductOrderController {
    @Autowired
    private ProductOrderService productOrderService;
    @Autowired
    private PayFactory payFactory;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductOrderItemService productOrderItemService;


    @PostMapping("/confirm")
    public void confirmOrder(@RequestBody ConfirmOrderDto confirmOrderDto, HttpServletResponse response) {
        String outTradeNo = null;
        try {
            JsonData jsonData = productOrderService.confirmOrder(confirmOrderDto);
            if (jsonData.getCode() == 0) {
                outTradeNo = jsonData.getData().toString();
                String clientType = confirmOrderDto.getClientType();
                String payType = confirmOrderDto.getPayType();
                if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())) {
                    PayInfoDTO payInfoDTO = new PayInfoDTO();
                    payInfoDTO.setClientType(clientType);
                    payInfoDTO.setPayType(payType);
                    payInfoDTO.setPayFee(confirmOrderDto.getRealPayAmount());
                    payInfoDTO.setTitle("光线传媒股票");
                    payInfoDTO.setOutTradeNo(outTradeNo);
                    String pay = payFactory.pay(payInfoDTO);
                    redisTemplate.opsForValue().set(outTradeNo, pay, 30, TimeUnit.MINUTES);
                    writePayPage(response, pay);
                } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name())) {
                    writePayPage(response, "");
                } else if (payType.equalsIgnoreCase(ProductOrderPayTypeEnum.BANK.name())) {
                    writePayPage(response, "");
                }
            } else {
                log.error("创建订单失败{}", jsonData.toString());
                writeJsonResponse(response, jsonData);
            }
        } catch (BizException e) {
            log.error("订单业务异常", e);
            if (outTradeNo != null) {
                productOrderService.cancelOrderAndUnlockCoupon(outTradeNo, confirmOrderDto.getCouponRecordId());
            }
            writeJsonResponse(response, JsonData.buildCodeAndMsg(e.getCode(), e.getMsg()));
        } catch (Exception e) {
            log.error("订单确认/支付过程发生异常", e);
            if (outTradeNo != null) {
                productOrderService.cancelOrderAndUnlockCoupon(outTradeNo, confirmOrderDto.getCouponRecordId());
            }
            writeJsonResponse(response, JsonData.buildResult(BizCodeEnum.ORDER_ERROR));
        }
    }

    @GetMapping("/page")
    public JsonData pageOrder(@RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.Login_ERROR);
        }
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<ProductOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductOrderDO::getUserId, loginUser.getId());
        wrapper.orderByDesc(ProductOrderDO::getCreateTime);
        productOrderService.page(pageInfo, wrapper);
        return JsonData.buildSuccess(pageInfo);
    }

    @GetMapping("/admin_page")
    public JsonData adminPageOrder(@RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<ProductOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProductOrderDO::getCreateTime);
        productOrderService.page(pageInfo, wrapper);
        return JsonData.buildSuccess(pageInfo);
    }

    @GetMapping("/cancel/{out_trade_no}")
    public JsonData cancelOrder(@PathVariable("out_trade_no") String outTradeNo) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.Login_ERROR);
        }
        LambdaQueryWrapper<ProductOrderDO> query = new LambdaQueryWrapper<>();
        query.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
        query.eq(ProductOrderDO::getUserId, loginUser.getId());
        ProductOrderDO one = productOrderService.getOne(query);
        if (one == null) {
            return JsonData.buildResult(BizCodeEnum.ORDER_ERROR);
        }
        if (!"NEW".equalsIgnoreCase(one.getState())) {
            return JsonData.buildResult(BizCodeEnum.ORDER_ERROR);
        }
        LambdaUpdateWrapper<ProductOrderDO> update = new LambdaUpdateWrapper<>();
        update.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
        update.set(ProductOrderDO::getState, ProductOrderStateEnum.CANCEL.name());
        productOrderService.update(update);
        productOrderService.cancelOrderAndUnlockCoupon(outTradeNo, null);
        return JsonData.buildSuccess();
    }

    @GetMapping("/detail/{out_trade_no}")
    public JsonData orderDetail(@PathVariable("out_trade_no") String outTradeNo) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null) return JsonData.buildResult(BizCodeEnum.Login_ERROR);
        LambdaQueryWrapper<ProductOrderDO> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
        if (loginUser.getAdmin() == null || loginUser.getAdmin() != 1) {
            orderQuery.eq(ProductOrderDO::getUserId, loginUser.getId());
        }
        ProductOrderDO order = productOrderService.getOne(orderQuery);
        if (order == null) return JsonData.buildResult(BizCodeEnum.ORDER_ERROR);
        LambdaQueryWrapper<ProductOrderItemDO> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(ProductOrderItemDO::getOutTradeNo, outTradeNo);
        List<ProductOrderItemDO> items = productOrderItemService.list(itemQuery);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return JsonData.buildSuccess(result);
    }

    private void writeJsonResponse(HttpServletResponse response, JsonData jsonData) {
        try {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSON.toJSONString(jsonData));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception ignored) {
        }
    }

    @GetMapping("/query_state")
    public JsonData queryProductOrderState(@RequestParam("out_trade_no") String outTradeNo) {
        return productOrderService.queryProductOrderState(outTradeNo);
    }

    private void writePayPage(HttpServletResponse response, String string) {
        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(string);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * @description:重新支付
     * @author: LZX
     * @date: 2025/4/19 14:56
     * @param: [outTradeNo]
     * @return: com.buka.util.JsonData
     **/
    @GetMapping("repay")
    public JsonData repay(@RequestParam("out_trade_no") String outTradeNo) {
        String pay = redisTemplate.opsForValue().get(outTradeNo);
        //不为空（未到过期时间）则直接返回支付页面
        if (pay != null) {
            return JsonData.buildSuccess(pay);
        }
        return JsonData.buildResult(BizCodeEnum.ORDER_PAY_TIME_OUT);

    }
    @GetMapping("get_token")
    public JsonData getToken() {
        //生成32位随机数
        String stringNumRandom = CommonUtil.getStringNumRandom(32);
        //获取当前登录用户
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        //将防重令牌传入redis
        String key= String.format(CacheKey.SUBMIT_ORDER_TOKEN_KEY, loginUser.getId());
        redisTemplate.opsForValue().set(key, stringNumRandom, 20, TimeUnit.MINUTES);
        return JsonData.buildSuccess(stringNumRandom);
    }
}

