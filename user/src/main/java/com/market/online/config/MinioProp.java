package com.market.online.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProp {
    //连接url
    private String endpoint;
    //公钥
    private String accessKey;
    //私钥
    private  String secretKey;
    //桶名称
    private String bucket;
    //浏览器访问地址
    private String publicEndpoint;
}
