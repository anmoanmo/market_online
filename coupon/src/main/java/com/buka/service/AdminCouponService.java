package com.buka.service;

import com.buka.request.DistributeCouponRequest;
import com.buka.util.JsonData;

public interface AdminCouponService {
    JsonData distributeBatch(DistributeCouponRequest request);
    JsonData distributeSingle(Long userId, Long couponId, String endTime);
}
