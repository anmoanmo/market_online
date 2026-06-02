package com.market.online.service;

import com.market.online.request.DistributeCouponRequest;
import com.market.online.util.JsonData;

public interface AdminCouponService {
    JsonData distributeBatch(DistributeCouponRequest request);
    JsonData distributeSingle(Long userId, Long couponId, String endTime);
}
