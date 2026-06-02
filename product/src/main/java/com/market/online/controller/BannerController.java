package com.market.online.controller;

import com.market.online.model.BannerDO;
import com.market.online.service.BannerService;
import com.market.online.util.AuthUtil;
import com.market.online.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner/v1")
public class BannerController {
    @Autowired
    private BannerService bannerService;

    @GetMapping("/list_banner")
    public JsonData list() {
        return JsonData.buildSuccess(bannerService.list());
    }

    @PostMapping("/add_banner")
    public JsonData add(@RequestBody BannerDO bannerDO) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        bannerService.save(bannerDO);
        return JsonData.buildSuccess();
    }

    @PutMapping("/update_banner")
    public JsonData update(@RequestBody BannerDO bannerDO) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        bannerService.updateById(bannerDO);
        return JsonData.buildSuccess();
    }

    @DeleteMapping("/delete_banner/{id}")
    public JsonData delete(@PathVariable("id") Long id) {
        JsonData denied = AuthUtil.requireAdmin();
        if (denied != null) return denied;
        bannerService.removeById(id);
        return JsonData.buildSuccess();
    }
}
