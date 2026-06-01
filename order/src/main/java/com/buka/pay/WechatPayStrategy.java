package com.buka.pay;

import com.buka.model.PayInfoDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;




@Slf4j
@Service
public class WechatPayStrategy implements PayStrategy {

    @Override
    public String unifiedorder(PayInfoDTO payInfoVO) {


        return null;
    }

    @Override
    public String refund(PayInfoDTO payInfoVO) {
        return null;
    }

    @Override
    public String queryPaySuccess(PayInfoDTO payInfoVO) {
        return null;
    }
}