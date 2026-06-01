package com.buka.feign;


import com.buka.request.LockCouponRecordRequest;
import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service")
public interface CouponFeignService {

    @GetMapping("/api/couponRecord/v1/detail/{record_id}")
    JsonData detailCouponRecord(@PathVariable("record_id") Long recordId);

    @PostMapping("/api/couponRecord/v1/lock_records")
    JsonData lockRecords(@RequestBody LockCouponRecordRequest lockRecords);

    @GetMapping("/api/couponRecord/v1/unlock/{record_id}")
    JsonData unlockCouponRecord(@PathVariable("record_id") Long recordId);

    @GetMapping("/api/couponRecord/v1/unlock_by_out_trade_no/{out_trade_no}")
    JsonData unlockByOutTradeNo(@PathVariable("out_trade_no") String outTradeNo);

    @GetMapping("/api/couponRecord/v1/confirm_by_out_trade_no/{out_trade_no}")
    JsonData confirmCouponByOutTradeNo(@PathVariable("out_trade_no") String outTradeNo);
}
