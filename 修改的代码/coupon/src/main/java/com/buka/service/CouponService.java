package com.buka.service;

import com.buka.model.CouponDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LZX
 * @since 2025-02-14
 */
public interface CouponService extends IService<CouponDO> {

    JsonData pageCoupon(Long page, Long size);

    JsonData adminPageCoupon(Long page, Long size);

    JsonData addPromotion(Long id);

    JsonData addCoupon(CouponDO couponDO);

    JsonData updateCoupon(CouponDO couponDO);
}
