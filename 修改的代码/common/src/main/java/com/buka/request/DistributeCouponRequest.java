package com.buka.request;

import lombok.Data;

@Data
public class DistributeCouponRequest {
    private Long couponId;
    private String endTime;
    private String registerDate;
}
