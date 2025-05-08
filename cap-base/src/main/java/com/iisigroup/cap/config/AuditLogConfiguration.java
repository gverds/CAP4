package com.iisigroup.cap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import com.iisigroup.cap.base.aop.CapAuditLog4HandlerAdvice;

/**
 * Spring AOP Configuration for Audit Logging
 * Compatible with Spring Security 6.4.4
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuditLogConfiguration {

    @Value("${systemId}")
    private String systemId;

    @Bean
    @Primary
    public CapAuditLog4HandlerAdvice auditLog4HandlerAdvice() {
        CapAuditLog4HandlerAdvice advice = new CapAuditLog4HandlerAdvice();
        advice.setSysId(systemId);
        return advice;
    }
    
    // 不要在這裡創建 AuditLogAspect bean，讓 Spring 自動探測它
    // 移除 @Bean 方法避免重複註冊
}