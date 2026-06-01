package com.buka.service;

import com.buka.dto.AddressAddDto;
import com.buka.model.AddressDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;

/**
 * <p>
 * 电商-公司收发货地址表 服务类
 * </p>
 *
 * @author zyd
 * @since 2025-02-09
 */
public interface AddressService extends IService<AddressDO> {

    JsonData add(AddressAddDto addressAddDto);

    JsonData deleteAdd(Long id);

    JsonData updateAdd(Long id, AddressAddDto addressAddDto);

    JsonData findUserAllAddress();
}
