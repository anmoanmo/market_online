package com.buka.controller;

import com.buka.request.DistributeCouponRequest;
import com.buka.service.AdminCouponService;
import com.buka.util.AuthUtil;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupon/v1")
public class AdminCouponController {

    @Autowired
    private AdminCouponService adminCouponService;

    @PostMapping("/distribute/batch")
    public JsonData distributeBatch(@RequestBody DistributeCouponRequest request) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return adminCouponService.distributeBatch(request);
    }

    @PostMapping("/distribute/single/{userId}")
    public JsonData distributeSingle(@PathVariable("userId") Long userId,
                                      @RequestParam("couponId") Long couponId,
                                      @RequestParam("endTime") String endTime) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return adminCouponService.distributeSingle(userId, couponId, endTime);
    }
}
