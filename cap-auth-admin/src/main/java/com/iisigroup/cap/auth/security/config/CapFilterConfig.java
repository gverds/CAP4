package com.iisigroup.cap.auth.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import com.iisigroup.cap.mvc.provider.SwitchProviderFilter;
import com.iisigroup.cap.security.filter.CapAnonymousAuthenticationFilter;
import com.iisigroup.cap.security.web.EnhancedRedirectStrategy;
import com.iisigroup.cap.utils.CapAppContext;

/**
 * 自訂 Filter
 */
@Configuration
public class CapFilterConfig {

	@Bean
	public SwitchProviderFilter switchProviderFilter(AuthenticationManager authenticationManager) {

		SwitchProviderFilter filter = new SwitchProviderFilter();
		filter.setAuthenticationManager(authenticationManager);
		filter.setAuthenticationSuccessHandler(CapAppContext.getBean("ajaxAuthenticationSuccessHandler"));
		filter.setAuthenticationFailureHandler(CapAppContext.getBean("ajaxAuthenticationFailureHandler"));
		filter.setFilterProcessesUrl("/j_spring_security_check");
		return filter;
	}

	@Bean
	public CapAnonymousAuthenticationFilter anonymousAuthFilter() {
		return new CapAnonymousAuthenticationFilter("cap");
	}

	@Bean
	public EnhancedRedirectStrategy enhancedRedirectStrategy() {
		return new EnhancedRedirectStrategy();
	}

}
