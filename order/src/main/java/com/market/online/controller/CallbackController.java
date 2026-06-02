package com.market.online.controller;

import com.alipay.api.AlipayConfig;
import com.alipay.api.internal.util.AlipaySignature;
import com.market.online.service.ProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping("/api/callback/v1")
public class CallbackController {


    @Value("${alibabaPay.alipayPublicKey}")
    private String ALIPAY_PUBLIC_KEY;


    @Autowired
    private ProductOrderService productOrderService;


    @PostMapping("/alipay")
    public String alipay(HttpServletRequest request) {
        //取出全部request中的值
        Map<String, String> Map = convertRequestParamsToMap(request);
        log.info("支付宝回调通知结果:{}", Map);
        //验证是否为支付宝发来的信息
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(Map, ALIPAY_PUBLIC_KEY, "UTF-8", "RSA2");//调用SDK验证签名

            if (signVerified) {
                System.out.println("签名验证成功");
                boolean flag = productOrderService.handlerOrderCallbackMsg(Map);
                if (flag) {
                    return "success";
                }

            } else {
                System.out.println("签名验证失败");
                return "failure";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }


        return "failure";
    }

    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();
        Set<Map.Entry<String, String[]>> entries = request.getParameterMap().entrySet();
        //获取所有键值对
        for (Map.Entry<String, String[]> entry : entries) {
            String name = entry.getKey();
            //不为空则返回
            String[] values = entry.getValue();
            int size = values.length;
            if (size == 1) {
                params.put(name, values[0]);
            } else {
                params.put(name, "");
            }
        }
        return params;
    }

}
