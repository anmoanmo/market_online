package com.buka.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.buka.enums.CouponStateEnum;
import com.buka.enums.StockTaskStateEnum;
import com.buka.model.CouponRecordDO;
import com.buka.model.CouponTaskDO;
import com.buka.request.LockCouponRecordRequest;
import com.buka.request.NewUserCouponRequest;
import com.buka.service.CouponRecordService;
import com.buka.service.CouponTaskService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LZX
 * @since 2025-02-14
 */
@RestController
@RequestMapping("/api/couponRecord/v1")
public class CouponRecordController {
    @Autowired
    private CouponRecordService couponRecordService;
    @Autowired
    private CouponTaskService couponTaskService;

    @GetMapping("/page_couponRecord")
    public JsonData selectCouponRecord(@RequestParam(value = "page", defaultValue = "1") Long page, @RequestParam(value = "size", defaultValue = "10") Long size) {
        return couponRecordService.selectCouponRecord(page, size);
    }

    @GetMapping("/detail/{record_id}")
    public JsonData detailCouponRecord(@PathVariable("record_id") Long recordId) {
        return couponRecordService.detailCouponRecord(recordId);
    }

    @PostMapping("/new_user_coupon")
    public JsonData newUserCoupon(@RequestBody NewUserCouponRequest newUserCouponRequest) {
        return couponRecordService.newUserCoupon(newUserCouponRequest);
    }
    @PostMapping("lock_records")
    public JsonData lockRecords(@RequestBody LockCouponRecordRequest lockRecords) {
        return couponRecordService.lockRecords(lockRecords);
    }

    @GetMapping("/unlock/{record_id}")
    public JsonData unlockCouponRecord(@PathVariable("record_id") Long recordId) {
        try {
            LambdaUpdateWrapper<CouponRecordDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(CouponRecordDO::getId, recordId);
            wrapper.set(CouponRecordDO::getUseState, CouponStateEnum.NEW.name());
            couponRecordService.update(wrapper);
            return JsonData.buildSuccess();
        } catch (Exception e) {
            return JsonData.buildError("解锁优惠券失败");
        }
    }

    @GetMapping("/unlock_by_out_trade_no/{out_trade_no}")
    public JsonData unlockByOutTradeNo(@PathVariable("out_trade_no") String outTradeNo) {
        try {
            LambdaQueryWrapper<CouponTaskDO> taskQuery = new LambdaQueryWrapper<>();
            taskQuery.eq(CouponTaskDO::getOutTradeNo, outTradeNo);
            CouponTaskDO task = couponTaskService.getOne(taskQuery);
            if (task != null) {
                LambdaUpdateWrapper<CouponRecordDO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(CouponRecordDO::getId, task.getCouponRecordId());
                wrapper.set(CouponRecordDO::getUseState, CouponStateEnum.NEW.name());
                couponRecordService.update(wrapper);
                task.setLockState(StockTaskStateEnum.CANCEL.name());
                couponTaskService.updateById(task);
            }
            return JsonData.buildSuccess();
        } catch (Exception e) {
            return JsonData.buildError("解锁优惠券失败");
        }
    }

    @GetMapping("/confirm_by_out_trade_no/{out_trade_no}")
    public JsonData confirmByOutTradeNo(@PathVariable("out_trade_no") String outTradeNo) {
        try {
            LambdaQueryWrapper<CouponTaskDO> taskQuery = new LambdaQueryWrapper<>();
            taskQuery.eq(CouponTaskDO::getOutTradeNo, outTradeNo);
            CouponTaskDO task = couponTaskService.getOne(taskQuery);
            if (task != null) {
                task.setLockState(StockTaskStateEnum.FINISH.name());
                couponTaskService.updateById(task);
            }
            return JsonData.buildSuccess();
        } catch (Exception e) {
            return JsonData.buildError("确认优惠券失败");
        }
    }
}

