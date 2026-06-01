package com.buka.request;

import lombok.Data;

@Data
public class OrderItemRequest {

    private Long productId;

    private Integer buyNum;
    
}