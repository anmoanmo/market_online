package com.market.online.controller;


import com.market.online.enums.BizCodeEnum;
import com.market.online.model.ProductDO;
import com.market.online.request.LockProductRequest;
import com.market.online.request.OrderItemRequest;
import com.market.online.service.ProductService;
import com.market.online.util.AuthUtil;
import com.market.online.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LZX
 * @since 2025-02-17
 */
@RestController
@RequestMapping("/api/product/v1")
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * @description:分页查询商品
     * @author: LZX
     * @date: 2025/4/21 21:50
     * @param: [page, size]
     * @return: com.market.online.util.JsonData
     **/
    @GetMapping("/page_product")
    public JsonData pageProduct(@RequestParam(value = "page", defaultValue = "1") Long page, @RequestParam(value = "size", defaultValue = "10") Long size) {
        return productService.pageProduct(page, size);
    }

    /**
     * @description:查询商品详细信息
     * @author: LZX
     * @date: 2025/4/21 21:50
     * @param: [productId]
     * @return: com.market.online.util.JsonData
     **/
    @GetMapping("/detail/{product_id}")
    public JsonData detailProduct(@PathVariable("product_id") Long productId) {
        return productService.detailProduct(productId);
    }
    /**
     * @description:锁定库存
     * @author: LZX
     * @date: 2025/4/21 21:50
     * @param: [lockProductRequest]
     * @return: com.market.online.util.JsonData
     **/
    @PostMapping("/lock_product")
    public JsonData lockProduct(@RequestBody LockProductRequest lockProductRequest) {
        return productService.lockProduct(lockProductRequest);
    }
    /**
     * @description:添加商品
     * @author: LZX
     * @date: 2025/4/21 21:50
     * @param: [productDO]
     * @return: com.market.online.util.JsonData
     **/
    @PostMapping("/add_product")
    public JsonData addProduct(@RequestBody ProductDO productDO) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        return productService.addProduct(productDO);
    }

    @PostMapping("/echo")
    public JsonData echo(@RequestBody ProductDO productDO) {
        return JsonData.buildSuccess(productDO);
    }

    @PutMapping("/update_product")
    public JsonData updateProduct(@RequestBody ProductDO productDO) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        try {
            if (productDO == null || productDO.getId() == null) {
                return JsonData.buildResult(BizCodeEnum.OPS_ERROR);
            }
            productService.updateById(productDO);
            return JsonData.buildSuccess();
        } catch (Exception e) {
            return JsonData.buildError(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/delete_product/{product_id}")
    public JsonData deleteProduct(@PathVariable("product_id") Long productId) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        productService.removeById(productId);
        return JsonData.buildSuccess();
    }

    /**
     * @description:模糊搜索商品
     * @author: LZX
     * @date: 2025/4/21 21:51
     * @param: [keyword, minPrice, maxPrice]
     * @return: com.market.online.util.JsonData
     **/
    @GetMapping("search")
    public JsonData search(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice){
        return productService.searchProducts(keyword, minPrice, maxPrice);

    }
}

