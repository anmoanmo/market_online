package com.market.online.config;

import com.market.online.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @className: InterceptorConfig
 * @author: LZX
 * @date: 2025/2/14 11:32
 * @Version: 1.0
 * @description:
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {
    public LoginInterceptor getLoginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor( getLoginInterceptor())
                .addPathPatterns("/api/user/v1/**","/api/address/v1/**")
                .excludePathPatterns("/api/user/*/send_code","/api/user/*/captcha",
                        "/api/user/*/register","/api/user/*/login","/api/user/*/upload");
    }
}


