package com.buka.service;

import com.buka.dto.ConfirmOrderDto;
import com.buka.model.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.util.JsonData;
import com.buka.vo.OrderAddrVo;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LZX
 * @since 2025-02-19
 */
public interface ProductOrderService extends IService<ProductOrderDO> {

    JsonData confirmOrder(ConfirmOrderDto confirmOrderDto);

    JsonData queryProductOrderState(String outTradeNo);

    OrderAddrVo getUserAddr(Long addressId);

    boolean closeProductOrder(String outTradeNo);

    boolean handlerOrderCallbackMsg(Map<String, String> paramsMap);

    void cancelOrderAndUnlockCoupon(String outTradeNo, Long couponRecordId);
}
