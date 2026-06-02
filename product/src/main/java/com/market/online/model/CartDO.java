package com.market.online.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * @className: CartDO
 * @author: LZX
 * @date: 2025/2/18 09:34
 * @Version: 1.0
 * @description:
 */

public class CartDO {
    private Integer totalNum;
    private BigDecimal totalAmount;
    private BigDecimal realPayAmount;
    private List<CartItemDO> cartItem;

    public Integer getTotalNum() {
        if (this.cartItem != null) {
           return this.totalNum = this.cartItem.size();
        }
        return 0;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal(0);
        for (CartItemDO item : cartItem) {
            totalAmount = totalAmount.add(item.getTotalAmount());
        }
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRealPayAmount() {
        BigDecimal realPayAmount = new BigDecimal(0);
        for (CartItemDO item : cartItem) {
            realPayAmount = realPayAmount.add(item.getTotalAmount());
        }
        return realPayAmount;
    }

    public void setRealPayAmount(BigDecimal realPayAmount) {
        this.realPayAmount = realPayAmount;
    }

    public List<CartItemDO> getCartItem() {
        return cartItem;
    }

    public void setCartItem(List<CartItemDO> cartItem) {
        this.cartItem = cartItem;
    }
}


