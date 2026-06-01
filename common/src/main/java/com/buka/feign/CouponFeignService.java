package com.buka.feign;


import com.buka.request.NewUserCouponRequest;
import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "coupon-service")
public interface CouponFeignService {
    @PostMapping("/api/couponRecord/v1/new_user_coupon")
    JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
