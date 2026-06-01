package com.buka.service;

import com.buka.dto.CartItemDto;
import com.buka.util.JsonData;

import java.util.List;

public interface CartService {
    JsonData addCart(CartItemDto cartItemDto);

    JsonData clearCart();

    JsonData getCart();

    JsonData changeCart(CartItemDto cartItemDto);

    JsonData delCart(Long productId);

    JsonData confirmOrderCartItems(List<Long> productIdList);

    JsonData clearByProductIds(List<Long> productIdList);
}
