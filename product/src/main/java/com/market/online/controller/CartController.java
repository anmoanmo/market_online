package com.market.online.controller;

import com.market.online.dto.CartItemDto;
import com.market.online.service.CartService;
import com.market.online.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @className: CartController
 * @author: LZX
 * @date: 2025/2/18 10:22
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/api/cart/v1")
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * @description:添加商品
     * @author: LZX
     * @date: 2025/2/20 10:05
     * @param: [cartItemDto]
     * @return: com.market.online.util.JsonData
     **/
    @PostMapping("/add")
    public JsonData addCart(@RequestBody CartItemDto cartItemDto) {
        return cartService.addCart(cartItemDto);
    }

    /**
     * @description:清空购物车
     * @author: LZX
     * @date: 2025/2/20 10:05
     * @param: []
     * @return: com.market.online.util.JsonData
     **/
    @DeleteMapping("/clear")
    public JsonData clearCart() {
        return cartService.clearCart();
    }

    /**
     * @description:获取购物车商品
     * @author: LZX
     * @date: 2025/2/20 10:05
     * @param: []
     * @return: com.market.online.util.JsonData
     **/
    @GetMapping("/myCart")
    public JsonData getCart() {
        return cartService.getCart();
    }

    /**
     * @description:更新购物车的商品购买数量
     * @author: LZX
     * @date: 2025/2/20 10:05
     * @param: [cartItemDto]
     * @return: com.market.online.util.JsonData
     **/
    @PutMapping("/change")
    public JsonData changeCart(@RequestBody CartItemDto cartItemDto) {
        return cartService.changeCart(cartItemDto);
    }

    /**
     * @description:删除商品
     * @author: LZX
     * @date: 2025/2/20 10:06
     * @param: [productId]
     * @return: com.market.online.util.JsonData
     **/
    @DeleteMapping("/del/{product_id}")
    public JsonData delCart(@PathVariable("product_id") Long productId) {
        return cartService.delCart(productId);
    }
    @PostMapping("/confirm_order_cart_items")
    public JsonData confirmOrderCartItems(@RequestBody List<Long> productIdList) {
        return cartService.confirmOrderCartItems(productIdList);
    }

    @PostMapping("/clear_by_product_ids")
    public JsonData clearByProductIds(@RequestBody List<Long> productIdList) {
        return cartService.clearByProductIds(productIdList);
    }
}


