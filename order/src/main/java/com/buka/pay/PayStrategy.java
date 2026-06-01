package com.buka.pay;


import com.buka.model.PayInfoDTO;

public interface PayStrategy {

    /**
     * 下单
     * @return
     */
    String unifiedorder(PayInfoDTO payInfoVO);


    /**
     *  退款
     * @param payInfoVO
     * @return
     */
    default String refund(PayInfoDTO payInfoVO){return "";}


    /**
     * 查询支付是否成功
     * @param payInfoVO
     * @return
     */
    default String queryPaySuccess(PayInfoDTO payInfoVO){return "";}

}
