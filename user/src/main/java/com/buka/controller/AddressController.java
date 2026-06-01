package com.buka.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buka.dto.AddressAddDto;
import com.buka.enums.BizCodeEnum;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.AddressDO;
import com.buka.service.AddressService;
import com.buka.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author zyd
 * @since 2025-02-09
 */
@RestController
@RequestMapping("/api/address/v1")
public class AddressController {
    @Autowired
    private AddressService addressService;

    /**
     * @description:测试方法
     * @author: LZX
     * @date: 2025/2/9 16:10
     * @param: null
     * @return: 全部的收获地址数据
     **/
    @RequestMapping("/test")
    public JsonData test() {
        List<AddressDO> list = null;
        try {
            list = addressService.list();
        } catch (Exception e) {
            return JsonData.buildResult(BizCodeEnum.OPS_ERROR);

        }
        return JsonData.buildSuccess(list);
    }
    /**
     * @description:新增地址
     * @author: LZX
     * @date: 2025/2/14 16:22
     * @param: [addressAddDto]
     * @return: com.buka.util.JsonData
     **/
    @PostMapping("/add")
    public JsonData add(@RequestBody AddressAddDto addressAddDto) {
        return addressService.add(addressAddDto);
    }

    @PutMapping("/update/{address_id}")
    public JsonData update(@PathVariable("address_id") Long id, @RequestBody AddressAddDto addressAddDto) {
        return addressService.updateAdd(id, addressAddDto);
    }
    /**
     * @description:通过地址id查找地址
     * @author: LZX
     * @date: 2025/2/14 16:23
     * @param: [id]
     * @return: com.buka.util.JsonData
     **/
    @GetMapping("/find/{address_id}")
    public JsonData find(@PathVariable("address_id") Long id) {
        LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressDO::getUserId, LoginInterceptor.threadLocal.get().getId());
        queryWrapper.eq(AddressDO::getId, id);
        if (addressService.getOne(queryWrapper) == null) {
            return JsonData.buildResult(BizCodeEnum.OPS_ERROR);
        }
        return JsonData.buildSuccess(addressService.getById(id));
    }
   /**
    * @description:通过地址id删除地址
    * @author: LZX
    * @date: 2025/2/14 16:23
    * @param: [id]
    * @return: com.buka.util.JsonData
    **/
    @DeleteMapping("/del/{address_id}")
    public JsonData del(@PathVariable("address_id") Long id) {
        return addressService.deleteAdd(id);
    }
    /**
     * @description:获取当前用户的所有地址
     * @author: LZX
     * @date: 2025/2/14 16:24
     * @param: []
     * @return: com.buka.util.JsonData
     **/
    @GetMapping("/list")
    public JsonData findUserAllAddress() {
        return addressService.findUserAllAddress();
    }
}

