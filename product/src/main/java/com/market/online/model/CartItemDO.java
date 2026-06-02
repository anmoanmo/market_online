package com.market.online.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @className: CartItemDO
 * @author: LZX
 * @date: 2025/2/18 09:47
 * @Version: 1.0
 * @description:
 */
public class CartItemDO {
    private Long productId;
    private Integer buyNum;
    private String productTitle;
    private BigDecimal productPrice;
    private String productImage;
    private BigDecimal totalAmount;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @JsonProperty("count")
    public Integer getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(Integer buyNum) {
        this.buyNum = buyNum;
    }

    @JsonProperty("productName")
    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    @JsonProperty("price")
    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    @JsonProperty("productImg")
    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = this.productPrice.multiply(BigDecimal.valueOf(this.buyNum));
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}


