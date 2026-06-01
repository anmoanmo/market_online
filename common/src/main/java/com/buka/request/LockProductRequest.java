package com.buka.request;

import lombok.Data;

import java.util.List;

@Data
public class LockProductRequest {

    private String orderOutTradeNo;

    private List<OrderItemRequest> orderItemRequest;
}