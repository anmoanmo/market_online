package com.market.online.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName CartItemVO
 * @date 2025/3/9 11:17
 */


public class CartItemVO {
    private Long productId;
    @JSONField(name = "count")
    private Integer buyNum;
    @JSONField(name = "productName")
    private String productTitle;
    @JSONField(name = "price")
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
    @JSONField(name = "count")
    public Integer getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(Integer buyNum) {
        this.buyNum = buyNum;
    }

    @JsonProperty("productName")
    @JSONField(name = "productName")
    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    @JsonProperty("productImg")
    @JSONField(name = "productImg")
    public String getProductImage() {
        return productImage;
    }

    @JSONField(name = "productImg")
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    @JsonProperty("price")
    @JSONField(name = "price")
    public BigDecimal getProductPrice() {
        return productPrice;
    }

    @JSONField(name = "price")
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public BigDecimal getTotalAmount() {
        if (this.productPrice == null || this.buyNum == null) {
            return BigDecimal.ZERO;
        }
        return this.productPrice.multiply(new BigDecimal(this.buyNum));
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
