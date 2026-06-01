package com.buka.config;

import com.buka.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @className: InterceptorConfig
 * @author: LZX
 * @date: 2025/2/15 10:29
 * @Version: 1.0
 * @description:
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {
    LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/couponRecord/*/**")
                .addPathPatterns("/api/coupon/*/**")
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/coupon/*/page_coupon","/api/couponRecord/*/new_user_coupon","/api/couponRecord/*/unlock/**","/api/couponRecord/*/detail/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}


