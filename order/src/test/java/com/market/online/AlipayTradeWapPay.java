package com.market.online;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.domain.ExtUserInfo;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.domain.ExtendParams;
import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.request.AlipayTradeWapPayRequest;

import com.alipay.api.FileItem;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

public class AlipayTradeWapPay {

    public static void main(String[] args) throws AlipayApiException {
        // 初始化SDK
        AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());

        // 构造请求参数以调用接口
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        
        // 设置商户订单号
        model.setOutTradeNo("70501111111S001111110");
        
        // 设置订单总金额
        model.setTotalAmount("66.00");
        
        // 设置订单标题
        model.setSubject("布卡java课程");
        
        // 设置产品码
        model.setProductCode("java01");

        // 设置用户付款中途退出返回商户网站的地址
        model.setQuitUrl("http://www.baidu.com");

        // 设置订单绝对超时时间
        model.setTimeExpire("2025-3-16 14:50:00");

        // 设置商户的原始订单号
        model.setMerchantOrderNo("20161008001");
        

        
        request.setBizModel(model);
        // 第三方代调用模式下请设置app_auth_token
        // request.putOtherTextParam("app_auth_token", "<-- 请填写应用授权令牌 -->");

        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
        // 如果需要返回GET请求，请使用
        // AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
        String pageRedirectionData = response.getBody();
        System.out.println("----------");
        System.out.println(pageRedirectionData);
        System.out.println("----------");
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }
    }

    private static AlipayConfig getAlipayConfig() {
        String privateKey = requireEnv("ALIPAY_PRIVATE_KEY");
        String alipayPublicKey = requireEnv("ALIPAY_PUBLIC_KEY");
        String appId = requireEnv("ALIPAY_APP_ID");
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(System.getenv().getOrDefault("ALIPAY_SERVER_URL", "https://openapi-sandbox.dl.alipaydev.com/gateway.do"));
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return value;
    }
}
