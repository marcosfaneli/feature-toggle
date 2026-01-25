package com.fnl33.featuretoggle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestCorrelationInterceptor requestCorrelationInterceptor;

    public WebMvcConfig(RequestCorrelationInterceptor requestCorrelationInterceptor) {
        this.requestCorrelationInterceptor = requestCorrelationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCorrelationInterceptor);
    }
}
