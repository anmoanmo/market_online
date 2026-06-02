package com.market.online.service;

import com.market.online.dto.ConfirmOrderDto;
import com.market.online.model.ProductOrderDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.market.online.util.JsonData;
import com.market.online.vo.OrderAddrVo;

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
