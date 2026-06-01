package com.buka.pay;

import com.buka.enums.ProductOrderPayTypeEnum;
import com.buka.model.PayInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class PayFactory {
    @Autowired
    private AlipayStrategy alipayStrategy;
    @Autowired
    private WechatPayStrategy wechatPayStrategy;


    public String pay(PayInfoDTO payInfoDTO){
        String payType = payInfoDTO.getPayType();

        if(ProductOrderPayTypeEnum.ALIPAY.name().equalsIgnoreCase(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(alipayStrategy);
            return payStrategyContext.executeUnifiedorder(payInfoDTO);
        } else if(ProductOrderPayTypeEnum.WECHAT.name().equalsIgnoreCase(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            return payStrategyContext.executeUnifiedorder(payInfoDTO);
        }

        return "";
    }


    public String queryPaySuccess(PayInfoDTO payInfoDTO) {
        String payType = payInfoDTO.getPayType();
        if(ProductOrderPayTypeEnum.ALIPAY.name().equalsIgnoreCase(payType)){
            //判断为支付宝支付
            PayStrategyContext payStrategyContext = new PayStrategyContext(alipayStrategy);
            return payStrategyContext.executeQueryPaySuccess(payInfoDTO);
        }else if(ProductOrderPayTypeEnum.WECHAT.name().equalsIgnoreCase(payType)){
            PayStrategyContext payStrategyContext = new PayStrategyContext(wechatPayStrategy);
            return payStrategyContext.executeQueryPaySuccess(payInfoDTO);
        }
        return null;
    }
}
