package com.market.online.feign;

import com.market.online.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignService {
    @GetMapping("/api/address/v1/find/{address_id}")
    JsonData find(@PathVariable("address_id") Long id);
}
