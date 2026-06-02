package com.market.online.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName CommonConfig
 * @date 2025/3/9 10:26
 */

@Configuration
@Slf4j
public class CommonConfig {


    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return template ->{
            //上下文对象（域对象）
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(requestAttributes!=null){
                HttpServletRequest request = requestAttributes.getRequest();
                if (null == request){
                    return;
                }
                log.info(request.getHeaderNames().toString());
                template.header("token", request.getHeader("token"));
            }else {
                log.warn("requestInterceptor获取Header空指针异常");
            }

        };
    }
}
