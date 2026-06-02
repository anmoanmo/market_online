package com.market.online.model;

import lombok.Data;

import java.util.List;

/**
 * @className: CouponPage
 * @author: LZX
 * @date: 2025/2/15 10:15
 * @Version: 1.0
 * @description:
 */
@Data
public class CouponPage<T> {
    private List<T> records;
    private Long total;
    private Long pages;
}


