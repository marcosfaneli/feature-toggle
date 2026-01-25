package com.fnl33.featuretoggle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.util.UUID;

@Component
@Slf4j
public class RequestCorrelationInterceptor implements HandlerInterceptor {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC = "correlationId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put(CORRELATION_ID_MDC, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        log.info("Request: method={} path={} correlationId={}", 
                request.getMethod(), 
                request.getRequestURI(),
                correlationId);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("Response: method={} path={} status={}", 
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus());
        
        MDC.clear();
    }
}
