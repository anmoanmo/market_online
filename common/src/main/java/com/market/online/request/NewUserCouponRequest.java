package com.market.online.request;

import lombok.Data;

/**
 * @className: NewUserCouponRequest
 * @author: LZX
 * @date: 2025/2/17 14:37
 * @Version: 1.0
 * @description:
 */
@Data
public class NewUserCouponRequest {
    private Long UserId;
    private String Name;
}