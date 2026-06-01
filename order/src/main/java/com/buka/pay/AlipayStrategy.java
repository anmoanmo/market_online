package com.buka.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.buka.exceptions.BizException;
import com.buka.model.PayInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class AlipayStrategy implements PayStrategy {
    @Value("${alibabaPay.NotifyUrl}")
    private String notifyUrl;
    @Value("${alibabaPay.returnUrl}")
    private String returnUrl;
    @Value("${alibabaPay.setServerUrl}")
    private String serverUrl;
    @Value("${alibabaPay.setAppId}")
    private String appId;
    @Value("${alibabaPay.privateKey}")
    private String privateKey;
    @Value("${alibabaPay.alipayPublicKey}")
    private String alipayPublicKey;


    @Override
    public String unifiedorder(PayInfoDTO payInfoVO) {
        // 初始化SDK
        String pageRedirectionData = null;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());

            // 构造请求参数以调用接口
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();

            // 设置商户订单号
            model.setOutTradeNo(payInfoVO.getOutTradeNo());

            // 设置订单总金额
            model.setTotalAmount(payInfoVO.getPayFee().toString());

            // 设置订单标题
            model.setSubject(payInfoVO.getTitle());
            LocalDateTime currentTime = LocalDateTime.now();

            LocalDateTime newTime = currentTime.plus(30, ChronoUnit.MINUTES);

// 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

// 格式化输出
            String formattedTime = newTime.format(formatter);

            // 设置订单绝对超时时间
            model.setTimeExpire(formattedTime);


            request.setBizModel(model);
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
            // 如果需要返回GET请求，请使用
            // AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
            pageRedirectionData = response.getBody();
            System.out.println("----------");
            System.out.println(pageRedirectionData);
            System.out.println("----------");
            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝调用失败");
        }
        return pageRedirectionData;
    }

    @Override
    public String refund(PayInfoDTO payInfoVO) {
        return PayStrategy.super.refund(payInfoVO);
    }

    @Override
    public String queryPaySuccess(PayInfoDTO payInfoVO) {
        try {
            // 初始化SDK
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            // 构造请求参数以调用接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            // 设置订单支付时传入的商户订单号
            model.setOutTradeNo(payInfoVO.getOutTradeNo());
            // 设置查询选项
            List<String> queryOptions = new ArrayList<String>();
            queryOptions.add("trade_settle_info");
            model.setQueryOptions(queryOptions);
            request.setBizModel(model);
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
            //返回当前订单状态,若未支付/订单不存在返回空,订单已支付返回页面
            return response.getTradeStatus();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("支付宝查询失败");
        }

    }


    private AlipayConfig getAlipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(requireConfig(serverUrl, "ALIPAY_SERVER_URL"));
        alipayConfig.setAppId(requireConfig(appId, "ALIPAY_APP_ID"));
        alipayConfig.setPrivateKey(requireConfig(privateKey, "ALIPAY_PRIVATE_KEY"));
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(requireConfig(alipayPublicKey, "ALIPAY_PUBLIC_KEY"));
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        return alipayConfig;
    }

    private String requireConfig(String value, String key) {
        if (value == null || value.trim().isEmpty() || value.startsWith("change-me")) {
            throw new IllegalStateException("缺少支付宝配置: " + key);
        }
        return value;
    }
}
