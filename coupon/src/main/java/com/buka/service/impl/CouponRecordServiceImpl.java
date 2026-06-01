package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.config.RabbitMQConfig;
import com.buka.enums.*;
import com.buka.exceptions.BizException;
import com.buka.feign.ProductOrderFeignService;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.CouponDO;
import com.buka.model.CouponRecordDO;
import com.buka.mapper.CouponRecordMapper;
import com.buka.model.CouponRecordMessage;
import com.buka.model.CouponTaskDO;
import com.buka.request.LockCouponRecordRequest;
import com.buka.request.NewUserCouponRequest;
import com.buka.service.CouponRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.service.CouponService;
import com.buka.service.CouponTaskService;
import com.buka.util.JsonData;
import com.buka.vo.CouponRecordVO;
import com.buka.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LZX
 * @since 2025-02-14
 */
@Service
@Slf4j
public class CouponRecordServiceImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordService {
    @Autowired
    private CouponService couponService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private CouponTaskService couponTaskService;
    @Autowired
    private ProductOrderFeignService productOrderFeignService;
    /**
     * @description:查询用户领取优惠券记录
     * @author: LZX
     * @date: 2025/2/17 11:25
     * @param: [page, size]
     * @return: com.buka.util.JsonData
     **/
    @Override
    public JsonData selectCouponRecord(Long page, Long size) {
        //获取当前登录用户
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        //创建分页
        Page<CouponRecordDO> page1 = new Page<>(page, size);
        //创建条件构造器
        LambdaQueryWrapper<CouponRecordDO> wrapper = new LambdaQueryWrapper<>();
        //查询对应用户的优惠券
        wrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
        //根据领取时间排序
        wrapper.orderByDesc(CouponRecordDO::getCreateTime);
        this.page(page1, wrapper);
        List<CouponRecordDO> list = page1.getRecords();
        long total = page1.getTotal();
        long pages = page1.getPages();
        //利用stream流将list中的数据复制到vo类并且重新封装成list
        List<CouponRecordVO> collect = list.stream().map(obj -> {
            CouponRecordVO couponRecordVO = new CouponRecordVO();
            BeanUtils.copyProperties(obj, couponRecordVO);
            return couponRecordVO;
        }).collect(Collectors.toList());
        //将三条数据封装成map集合返回
        //只需存三个值，设置数组长度为3
        Map<String, Object> map = new HashMap<>(3);
        map.put("total", total);
        map.put("pages", pages);
        map.put("records", collect);
        return JsonData.buildSuccess(map);
    }

    /**
     * @description:用户查询单个优惠券信息
     * @author: LZX
     * @date: 2025/2/17 14:33
     * @param: [recordId]
     * @return: com.buka.util.JsonData
     **/
    @Override
    public JsonData detailCouponRecord(Long recordId) {
        LambdaQueryWrapper<CouponRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponRecordDO::getId, recordId);
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser != null) {
            queryWrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
        }
        CouponRecordDO aDo = getOne(queryWrapper);
        return aDo==null?JsonData.buildError("优惠券记录出错"):JsonData.buildSuccess(aDo);
    }
/**
 * @description:新用户注册发放优惠券
 * @author: LZX
 * @date: 2025/2/19 16:56
 * @param: [newUserCouponRequest]
 * @return: com.buka.util.JsonData
 **/
    @Override
    public JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest) {
        //创建注册用户
        LoginUser loginUser = new LoginUser();
        //将前端传入的注册用户id和昵称封装
        loginUser.setId(newUserCouponRequest.getUserId());
        loginUser.setName(newUserCouponRequest.getName());
        //讲封装好的注册用户传入newUserCoupon
        LoginInterceptor.threadLocal.set(loginUser);
        //创建条件构造器
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        //限制已发布的优惠券
        wrapper.eq(CouponDO::getPublish, CouponPublishEnum.PUBLISH.name());
        //限制新用户优惠券
        wrapper.eq(CouponDO::getCategory, CouponCategoryEnum.NEW_USER.name());
        //查询所有符合新用户的优惠券
        List<CouponDO> couponDOList = couponService.list(wrapper);
        for (CouponDO couponDO : couponDOList) {
            couponService.addPromotion(couponDO.getId());
        }
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData lockRecords(LockCouponRecordRequest lockRecords) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null) {
            log.error("[优惠券微服务]-锁券失败, 未获取到登录用户");
            throw new BizException(BizCodeEnum.COUPON_LOCK_FAIL);
        }
        List<Long> ids = lockRecords.getCouponRecordIds();
        String outTradeNo = lockRecords.getOutTradeNo();
        for (Long id : ids) {
            LambdaUpdateWrapper<CouponRecordDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(CouponRecordDO::getId, id);
            wrapper.eq(CouponRecordDO::getUserId, loginUser.getId());
            wrapper.set(CouponRecordDO::getUseState, CouponStateEnum.USED.name());
            boolean update = update(wrapper);
            if (!update) {
                throw new BizException(BizCodeEnum.COUPON_LOCK_FAIL);
            }
            //成功则创建记录
            CouponTaskDO couponTaskDO = new CouponTaskDO();
            couponTaskDO.setCouponRecordId(id);
            couponTaskDO.setLockState(CouponStateEnum.LOCK.name());
            couponTaskDO.setCreateTime(new Date());
            couponTaskDO.setOutTradeNo(outTradeNo);
            couponTaskService.save(couponTaskDO);
            //发送消息
            CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
            couponRecordMessage.setOutTradeNo(outTradeNo);
            couponRecordMessage.setTaskId(couponTaskDO.getId());
            rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getCouponReleaseDelayRoutingKey(), couponRecordMessage);

        }
        return JsonData.buildSuccess();
    }

    @Override
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        Long taskId = recordMessage.getTaskId();
        String outTradeNo = recordMessage.getOutTradeNo();
        CouponTaskDO byId = couponTaskService.getById(taskId);
        if (byId == null) {
            log.warn("工作单不存，消息:{}",recordMessage);
            return true;
        }
        if (byId.getLockState().equalsIgnoreCase(CouponStateEnum.LOCK.name())) {
            JsonData jsonData = productOrderFeignService.queryProductOrderState(outTradeNo);
            if (jsonData.getCode() == 0) {
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(jsonData.getData()), Map.class);
                Object stateObj = map.get("state");
                String orderState = stateObj != null ? stateObj.toString() : "";
                if (orderState.equalsIgnoreCase(ProductOrderStateEnum.NEW.name())) {
                    log.warn("订单状态是NEW,返回给消息队列，重新投递:{}", recordMessage);
                    return false;
                }
                if (orderState.equalsIgnoreCase(ProductOrderStateEnum.PAY.name())) {
                    byId.setLockState(StockTaskStateEnum.FINISH.name());
                    couponTaskService.updateById(byId);
                    return true;
                }
            }
            //订单不存在或者已取消
            byId.setLockState(StockTaskStateEnum.CANCEL.name());
            couponTaskService.updateById(byId);
            //回滚优惠券数据库
            LambdaUpdateWrapper<CouponRecordDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(CouponRecordDO::getId, byId.getCouponRecordId());
            wrapper.set(CouponRecordDO::getUseState, CouponStateEnum.NEW.name());
            update(wrapper);
        }
        return true;
    }
}
