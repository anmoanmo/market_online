package com.buka.feign;

import com.buka.request.LockProductRequest;
import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author LZX
 * @version 1.0
 * @ClassName ProductFeignService
 * @date 2025/3/9 11:11
 */

@FeignClient("product-service")
public interface ProductFeignService {

    @PostMapping("/api/cart/v1/confirm_order_cart_items")
    JsonData confirmOrderCartItems(@RequestBody List<Long> productIdList);

    @PostMapping("/api/product/v1/lock_product")
    JsonData lockProduct(@RequestBody LockProductRequest lockProductRequest);

    @PostMapping("/api/cart/v1/clear_by_product_ids")
    JsonData clearCartByProductIds(@RequestBody List<Long> productIdList);
}
