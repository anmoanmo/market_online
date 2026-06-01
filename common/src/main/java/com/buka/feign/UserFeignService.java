package com.buka.feign;

import com.buka.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserFeignService {
    @GetMapping("/api/user/v1/list_by_register_date")
    JsonData listByRegisterDate(@RequestParam("date") String date);
}
