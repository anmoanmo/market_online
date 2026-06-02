package com.market.online.feign;


import com.market.online.request.NewUserCouponRequest;
import com.market.online.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "coupon-service")
public interface CouponFeignService {
    @PostMapping("/api/couponRecord/v1/new_user_coupon")
    JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
