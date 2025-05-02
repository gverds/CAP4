package com.iisigroup.cap.auth.security.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import com.iisigroup.cap.security.web.CapConcurrentSessionStrategy;

/**
 * session
 */
@Configuration
public class CapSessionManagerConfig {

	@Bean
	public CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new CompositeSessionAuthenticationStrategy(
				Arrays.asList(
						new CapConcurrentSessionStrategy(sessionRegistry()),
						new SessionFixationProtectionStrategy(),
						new RegisterSessionAuthenticationStrategy(sessionRegistry())
				)
			);
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	public CapConcurrentSessionStrategy concurrentSessionStrategy() {
		CapConcurrentSessionStrategy strategy = new CapConcurrentSessionStrategy(sessionRegistry());
		strategy.setMaximumSessions(1);
		strategy.setExceptionIfMaximumExceeded(false);
		return strategy;
	}

}
