package com.buka.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buka.enums.CouponStateEnum;
import com.buka.enums.BizCodeEnum;
import com.buka.feign.UserFeignService;
import com.buka.model.CouponDO;
import com.buka.model.CouponRecordDO;
import com.buka.request.DistributeCouponRequest;
import com.buka.service.AdminCouponService;
import com.buka.service.CouponRecordService;
import com.buka.service.CouponService;
import com.buka.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminCouponServiceImpl implements AdminCouponService {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRecordService couponRecordService;

    @Autowired
    private UserFeignService userFeignService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData distributeBatch(DistributeCouponRequest request) {
        if (request.getCouponId() == null || request.getEndTime() == null || request.getRegisterDate() == null) {
            return JsonData.buildError("参数错误");
        }
        CouponDO couponDO = couponService.getById(request.getCouponId());
        if (couponDO == null) {
            return JsonData.buildResult(BizCodeEnum.COUPON_NOT_EXIST);
        }
        if (couponDO.getStock() <= 0) {
            return JsonData.buildResult(BizCodeEnum.COUPON_NOT_STOCK);
        }

        JsonData jsonData = userFeignService.listByRegisterDate(request.getRegisterDate());
        if (jsonData.getCode() != 0) {
            return JsonData.buildError("获取用户列表失败");
        }
        String jsonString = JSON.toJSONString(jsonData.getData());
        List<Map<String, Object>> userList = JSON.parseObject(jsonString, new TypeReference<List<Map<String, Object>>>(){});

        if (userList == null || userList.isEmpty()) {
            return JsonData.buildSuccess("没有符合条件的用户");
        }

        Date endDate = parseEndTime(request.getEndTime());
        if (endDate == null) {
            return JsonData.buildError("参数错误");
        }

        int successCount = 0;
        for (Map<String, Object> user : userList) {
            Object idObj = user.get("id");
            Object nameObj = user.get("name");
            if (idObj == null) continue;
            Long userId = Long.valueOf(idObj.toString());
            String userName = nameObj != null ? nameObj.toString() : "";

            if (couponDO.getStock() <= 0) break;

            LambdaQueryWrapper<CouponRecordDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CouponRecordDO::getUserId, userId);
            wrapper.eq(CouponRecordDO::getCouponId, couponDO.getId());
            int count = couponRecordService.count(wrapper);
            if (count >= couponDO.getUserLimit()) continue;

            couponDO.setStock(couponDO.getStock() - 1);
            couponService.updateById(couponDO);

            CouponRecordDO record = new CouponRecordDO();
            record.setCouponId(couponDO.getId());
            record.setCreateTime(new Date());
            record.setUseState(CouponStateEnum.NEW.name());
            record.setUserId(userId);
            record.setUserName(userName);
            record.setCouponTitle(couponDO.getCouponTitle());
            record.setStartTime(couponDO.getStartTime());
            record.setEndTime(endDate);
            record.setPrice(couponDO.getPrice());
            record.setConditionPrice(couponDO.getConditionPrice());
            couponRecordService.save(record);
            successCount++;
        }

        return JsonData.buildSuccess("成功发放 " + successCount + " 张优惠券");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData distributeSingle(Long userId, Long couponId, String endTime) {
        CouponDO couponDO = couponService.getById(couponId);
        if (couponDO == null) {
            return JsonData.buildResult(BizCodeEnum.COUPON_NOT_EXIST);
        }
        if (couponDO.getStock() <= 0) {
            return JsonData.buildResult(BizCodeEnum.COUPON_NOT_STOCK);
        }

        LambdaQueryWrapper<CouponRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponRecordDO::getUserId, userId);
        wrapper.eq(CouponRecordDO::getCouponId, couponId);
        int count = couponRecordService.count(wrapper);
        if (count >= couponDO.getUserLimit()) {
            return JsonData.buildError("该用户已达到领取上限");
        }

        Date endDate = parseEndTime(endTime);
        if (endDate == null) {
            return JsonData.buildError("参数错误");
        }

        couponDO.setStock(couponDO.getStock() - 1);
        couponService.updateById(couponDO);

        CouponRecordDO record = new CouponRecordDO();
        record.setCouponId(couponDO.getId());
        record.setCreateTime(new Date());
        record.setUseState(CouponStateEnum.NEW.name());
        record.setUserId(userId);
        record.setUserName("");
        record.setCouponTitle(couponDO.getCouponTitle());
        record.setStartTime(couponDO.getStartTime());
        record.setEndTime(endDate);
        record.setPrice(couponDO.getPrice());
        record.setConditionPrice(couponDO.getConditionPrice());
        couponRecordService.save(record);

        return JsonData.buildSuccess("发放成功");
    }

    private Date parseEndTime(String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(endTime);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(endTime);
            } catch (ParseException ex) {
                return null;
            }
        }
    }
}
