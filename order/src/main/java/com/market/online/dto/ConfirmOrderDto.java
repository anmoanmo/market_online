package com.market.online.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @className: ConfirmOrderDto
 * @author: LZX
 * @date: 2025/2/19 10:32
 * @Version: 1.0
 * @description:
 */
@Data
public class ConfirmOrderDto {
    private Long couponRecordId;
    private List<Long> productIdList;
    private String payType;
    private String clientType;
    private Long addressId;
    private BigDecimal totalAmount;
    private BigDecimal realPayAmount;
    //防止重复提交令牌
    private String token;
}


