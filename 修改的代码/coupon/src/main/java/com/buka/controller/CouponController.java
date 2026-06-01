package com.buka.controller;


import com.buka.model.CouponDO;
import com.buka.service.CouponService;
import com.buka.util.AuthUtil;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/api/coupon/v1")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @GetMapping("/page_coupon")
    public JsonData pageCoupon(@RequestParam(value = "page", defaultValue = "1") Long page, @RequestParam(value = "size", defaultValue = "10") Long size) {
        return couponService.pageCoupon(page,size);
    }

    @GetMapping("/admin_page")
    public JsonData adminPageCoupon(@RequestParam(value = "page", defaultValue = "1") Long page, @RequestParam(value = "size", defaultValue = "10") Long size) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return couponService.adminPageCoupon(page,size);
    }

    @PutMapping("/add/promotion/{coupon_id}")
    public JsonData addPromotion(@PathVariable("coupon_id") Long id) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return couponService.addPromotion(id);
    }

    @PostMapping("/admin_add")
    public JsonData addCoupon(@RequestBody CouponDO couponDO) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return couponService.addCoupon(couponDO);
    }

    @PutMapping("/admin_update")
    public JsonData updateCoupon(@RequestBody CouponDO couponDO) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return couponService.updateCoupon(couponDO);
    }

    @RequestMapping("/test")
    public String test() {
        return couponService.list().toString();
    }
}

