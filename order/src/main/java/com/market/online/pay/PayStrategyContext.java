package com.market.online.pay;

import com.market.online.model.PayInfoDTO;




public class PayStrategyContext {

    private PayStrategy payStrategy;

    public PayStrategyContext(PayStrategy payStrategy) {
        this.payStrategy = payStrategy;
    }

    /**
     * 根据支付策略，调用不同的支付
     * @param payInfoVO
     * @return
     */
    public String executeUnifiedorder(PayInfoDTO payInfoVO){
        return this.payStrategy.unifiedorder(payInfoVO);
    }


    /**
     * 根据支付的策略，调用不同的查询订单支持状态
     * @param payInfoVO
     * @return
     */
    public String executeQueryPaySuccess(PayInfoDTO payInfoVO){
        return this.payStrategy.queryPaySuccess(payInfoVO);

    }
}
