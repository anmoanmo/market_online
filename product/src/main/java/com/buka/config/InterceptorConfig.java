package com.buka.config;

import com.buka.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**

 **/

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor())
                //拦截的路径
                .addPathPatterns("/api/cart/*/**")
                .addPathPatterns("/api/product/*/add_product")
                .addPathPatterns("/api/product/*/update_product")
                .addPathPatterns("/api/product/*/delete_product/**")
                .addPathPatterns("/api/banner/*/add_banner")
                .addPathPatterns("/api/banner/*/update_banner")
                .addPathPatterns("/api/banner/*/delete_banner/**")
                //排查不拦截的路径
                .excludePathPatterns("/api/product/**/lock_product");

    }
}
