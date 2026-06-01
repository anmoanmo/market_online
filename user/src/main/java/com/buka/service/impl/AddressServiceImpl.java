package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.buka.dto.AddressAddDto;
import com.buka.enums.BizCodeEnum;
import lombok.extern.slf4j.Slf4j;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.AddressDO;
import com.buka.mapper.AddressMapper;
import com.buka.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.JsonData;
import com.buka.vo.AddressInfoVo;
import com.buka.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 电商-公司收发货地址表 服务实现类
 * </p>
 *
 * @author zyd
 * @since 2025-02-09
 */
@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements AddressService {
    /**
     * @description:添加地址
     * @author: LZX
     * @date: 2025/2/14 14:32
     * @param: [addressAddDto]
     * @return: com.buka.util.JsonData
     **/
    @Override
    public JsonData add(AddressAddDto addressAddDto) {
        try {
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                return JsonData.buildResult(BizCodeEnum.Login_ERROR);
            }
            AddressDO addressDO = new AddressDO();
            addressDO.setReceiveName(addressAddDto.getReceiveName());
            addressDO.setPhone(addressAddDto.getPhone());
            addressDO.setProvince(addressAddDto.getProvince());
            addressDO.setCity(addressAddDto.getCity());
            addressDO.setRegion(addressAddDto.getRegion());
            addressDO.setDetailAddress(addressAddDto.getDetailAddress());
            addressDO.setUserId(loginUser.getId());
            addressDO.setCreateTime(new Date());
            if (addressAddDto.getDefaultStatus() != null && addressAddDto.getDefaultStatus() == 1) {
                LambdaUpdateWrapper<AddressDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(AddressDO::getUserId, loginUser.getId());
                updateWrapper.eq(AddressDO::getDefaultStatus, 1);
                updateWrapper.set(AddressDO::getDefaultStatus, 0);
                update(updateWrapper);
            }
            this.save(addressDO);
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("添加地址失败", e);
            throw e;
        }
    }

    @Override
    public JsonData updateAdd(Long id, AddressAddDto addressAddDto) {
        LambdaUpdateWrapper<AddressDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressDO::getId, id);
        wrapper.eq(AddressDO::getUserId, LoginInterceptor.threadLocal.get().getId());
        AddressDO addressDO = new AddressDO();
        addressDO.setReceiveName(addressAddDto.getReceiveName());
        addressDO.setPhone(addressAddDto.getPhone());
        addressDO.setProvince(addressAddDto.getProvince());
        addressDO.setCity(addressAddDto.getCity());
        addressDO.setRegion(addressAddDto.getRegion());
        addressDO.setDetailAddress(addressAddDto.getDetailAddress());
        update(addressDO, wrapper);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData deleteAdd(Long id) {
        AddressDO addressDO = getById(id);
        if (addressDO == null) {
            return JsonData.buildResult(BizCodeEnum.DELETE_ADDRESS_ERROR);
        }
        if (addressDO.getDefaultStatus() == 1) {
            LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AddressDO::getUserId, LoginInterceptor.threadLocal.get().getId());
            queryWrapper.eq(AddressDO::getDefaultStatus, 0);
            List<AddressDO> addressDOList = list(queryWrapper);
            if (addressDOList != null && addressDOList.size() != 0) {
                AddressDO address = addressDOList.get(0);
                address.setDefaultStatus(1);
                updateById(address);
                removeById(id);
                return JsonData.buildSuccess();
            }
            return JsonData.buildResult(BizCodeEnum.DELETE_ADDRESS_ERROR);
        }
        removeById(id);
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData findUserAllAddress() {
        LambdaQueryWrapper<AddressDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressDO::getUserId, LoginInterceptor.threadLocal.get().getId());
        List<AddressDO> list = list(queryWrapper);
        List<AddressInfoVo> addressInfoVos = list.stream().map(obg -> {
            AddressInfoVo addressInfoVo = new AddressInfoVo();
            BeanUtils.copyProperties(obg, addressInfoVo);
            return addressInfoVo;
        }).collect(Collectors.toList());
        return JsonData.buildSuccess(addressInfoVos);
    }
}
