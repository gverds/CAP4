package com.iisigroup.cap.auth.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

import com.iisigroup.cap.auth.provider.CapAuthenticationProvider;
import com.iisigroup.cap.auth.service.impl.AccessControlServiceImpl;
import com.iisigroup.cap.auth.service.impl.PasswordServiceImpl;
import com.iisigroup.cap.auth.service.impl.UserDetailsServiceImpl;
import com.iisigroup.cap.security.captcha.filter.CaptchaCaptureFilter;
import com.iisigroup.cap.utils.CapAppContext;

/**
 * 自訂 AuthenticationManager
 */
@Configuration
public class CapAuthManagerConfig {
	
	@Bean
	public AuthenticationManager authenticationManager(CapAuthenticationProvider capAuthenticationProvider) {
		return new ProviderManager(capAuthenticationProvider);
	}

	@Bean
	public CapAuthenticationProvider capAuthenticationProvider(CaptchaCaptureFilter captchaCaptureFilter, UserDetailsServiceImpl userDetailsServiceImpl) {
		CapAuthenticationProvider provider = new CapAuthenticationProvider();
        provider.setCaptchaCaptureFilter(captchaCaptureFilter);
		provider.setUserService(userDetailsServiceImpl);
		provider.setPasswordService(CapAppContext.getBean("passwordService"));
		provider.setAccessControlService(CapAppContext.getBean("accessControlService"));
		return provider;
	}
	
	@Bean
	public CaptchaCaptureFilter captchaCaptureFilter() {
		return new CaptchaCaptureFilter();
	}
	
	@Bean
	public UserDetailsServiceImpl userDetailsServiceImpl() {
		return new UserDetailsServiceImpl();
	}

}
