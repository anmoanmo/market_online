package com.market.online.request;

import lombok.Data;

import java.util.List;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName LockCouponRecordRequest
 * @date 2025/3/8 15:18
 */

@Data
public class LockCouponRecordRequest {
    private String outTradeNo;
    private List<Long> couponRecordIds;
}
