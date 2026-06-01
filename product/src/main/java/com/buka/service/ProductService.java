package com.buka.service;

import com.buka.model.ProductDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.mq.ProductMessage;
import com.buka.request.LockProductRequest;
import com.buka.util.JsonData;

import java.math.BigDecimal;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LZX
 * @since 2025-02-17
 */
public interface ProductService extends IService<ProductDO> {

    JsonData pageProduct(Long page, Long size);

    JsonData detailProduct(Long productId);

    JsonData lockProduct(LockProductRequest lockProductRequest);

    boolean releaseProductStock(ProductMessage productMessage);

    JsonData addProduct(ProductDO productDO);

    JsonData updateProduct(ProductDO productDO);

    JsonData deleteProduct(Long productId);

    JsonData searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice);
}
