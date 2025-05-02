package com.iisigroup.cap.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

public class CapAuthenticationEventLogger implements ApplicationListener<AbstractAuthenticationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CapAuthenticationEventLogger.class);

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
    	// 認證事件處理
        if (event instanceof AuthenticationSuccessEvent) {
            logger.info("登入成功 登入人員: {}; Details: {}", event.getAuthentication().getName(), event.getAuthentication().getDetails());
        } else if (event instanceof AbstractAuthenticationFailureEvent failureEvent) {
            logger.warn("登入失敗原因: {}; 登入人員： {}; Details: {}", failureEvent.getException(), failureEvent.getAuthentication().getName(), event.getAuthentication().getDetails());
        } else if (event instanceof InteractiveAuthenticationSuccessEvent) {
        	// 網頁登入
        	logger.info("登入成功 登入人員: {}; Details: {}", event.getAuthentication().getName(), event.getAuthentication().getDetails());
        } else {
            logger.debug("Authentication event: {}", event.getClass().getSimpleName());
        }
        // 原本 LoggerListener 裡面的授權事件都要棄用了
    }
}