package com.buka.feign;

import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @className: ProductOrderFeignService
 * @author: LZX
 * @date: 2025/3/8 20:27
 * @Version: 1.0
 * @description:
 */
@FeignClient(name = "order-service")
public interface ProductOrderFeignService {
    @GetMapping("/api/productOrder/v1/query_state")
    JsonData queryProductOrderState(@RequestParam("out_trade_no") String outTradeNo);
}


