package com.iisigroup.cap.auth.security.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.iisigroup.cap.auth.security.CapAuthenticationEventLogger;
import com.iisigroup.cap.auth.security.CapAuthorizationManager;
import com.iisigroup.cap.auth.security.CapSessionExpiredStrategy;
import com.iisigroup.cap.mvc.provider.SwitchProviderFilter;
import com.iisigroup.cap.security.CapSecurityContext;
import com.iisigroup.cap.security.filter.CapAnonymousAuthenticationFilter;
import com.iisigroup.cap.security.web.CapAuthenticationEntryPoint;
import com.iisigroup.cap.security.web.EnhancedRedirectStrategy;
import com.iisigroup.cap.utils.CapAppContext;

/**
 * 主安全配置
 */
@Configuration
@EnableWebSecurity
public class CapSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
											CapAuthorizationManager capAuthorizationManager,
											SessionInformationExpiredStrategy sessionInformationExpiredStrategy,
											SwitchProviderFilter switchProviderFilter,
											CapAnonymousAuthenticationFilter anonymousAuthFilter,
											CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy,
            								HandlerMappingIntrospector mvcHandlerMappingIntrospector, 
            								SessionRegistry sessionRegistry
	) throws Exception {
		// 設定忽略的 URL patterns（建議使用標準化的 URI，不含 query string）
	    Set<String> ignoreUris = new HashSet<>(
    		Arrays.asList(
				"i18nhandler", 
				"codetypehandler"
    		)
        );
	    capAuthorizationManager.setIgnoreHandlers(ignoreUris);

		http.securityContext(context -> context
				.requireExplicitSave(true))
				.csrf(csrf -> csrf
					.requireCsrfProtectionMatcher(csrfMatcher())
				)
				.authorizeHttpRequests(authz -> authz
					.requestMatchers(
							new AntPathRequestMatcher("/img/**"),
							new AntPathRequestMatcher("/**/images/**"),
							new AntPathRequestMatcher("/**/fonts/**"),
							new AntPathRequestMatcher("/jquery/**"),
							new AntPathRequestMatcher("/**/*.css"),
							new AntPathRequestMatcher("/**/*.js"),
							new AntPathRequestMatcher("/**/*.map"),
							new AntPathRequestMatcher("/i18njs*"),
							new AntPathRequestMatcher("/captcha.png*"),
							new AntPathRequestMatcher("/app/error/message"),
							new AntPathRequestMatcher("/page/login*"),
							new AntPathRequestMatcher("/page/error*"),
							new AntPathRequestMatcher("/page/home/*")).permitAll()
					// 以下需要完整認證
					.requestMatchers(
							new AntPathRequestMatcher("/page/index*"),
							new AntPathRequestMatcher("/page/common/*"),
							new AntPathRequestMatcher("/page/**"),
							new AntPathRequestMatcher("/**")).fullyAuthenticated()
	                .anyRequest().access(capAuthorizationManager)
				)
	            .addFilterBefore(switchProviderFilter, UsernamePasswordAuthenticationFilter.class)
	            .addFilterAfter(anonymousAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.sessionManagement(session -> session
					.sessionAuthenticationStrategy(sessionAuthenticationStrategy)
		            .maximumSessions(1)
		            .expiredSessionStrategy(sessionInformationExpiredStrategy) // session 被踢策略
	                .sessionRegistry(sessionRegistry)
					
				)
				.formLogin(form -> form
						.loginPage("/page/login")
						.loginProcessingUrl("/j_spring_security_check")
						.defaultSuccessUrl("/page/index", true)
						.failureUrl("/page/login?error")
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/j_spring_security_logout")
						.logoutSuccessUrl("/page/login")
						.permitAll()
				)
	            .exceptionHandling(exception -> exception
	                    .accessDeniedPage("/page/login?error")
	                    .authenticationEntryPoint(authenticationEntryPoint())
	                )
				.headers(headers -> headers
						.frameOptions(options -> options.sameOrigin()
				)
						
			);

		return http.build();
	}

	@Bean
	public RequestMatcher csrfMatcher() {
		return new AndRequestMatcher(
			CsrfFilter.DEFAULT_CSRF_MATCHER,
			new NegatedRequestMatcher(
				new OrRequestMatcher(
					new AntPathRequestMatcher("/api/**", "POST"),
					new AntPathRequestMatcher("/page/**", "POST"),
					new AntPathRequestMatcher("/**", "POST")
				)
			)
		);
	}

	@Bean
	public CapAuthenticationEntryPoint authenticationEntryPoint() {
		CapAuthenticationEntryPoint entryPoint = new CapAuthenticationEntryPoint("/page/login");
		entryPoint.setForceHttps(false);
		return entryPoint;
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public CapSecurityContext capSecurityContext() {
	    return new CapSecurityContext();
	}
	
//	@Bean
//	public LoggerListener loggerListener() {
//	    return new LoggerListener();
//	}
	
	@Bean
    public CapAuthenticationEventLogger authenticationEventLogger() {
		// LoggerListener 要棄用了
        return new CapAuthenticationEventLogger();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
	
	@Bean
	public CapAuthorizationManager capAuthorizationManager() {
		return new CapAuthorizationManager(CapAppContext.getBean("accessControlService"));
	}
	
	@Bean
	public SessionInformationExpiredStrategy sessionInformationExpiredStrategy(EnhancedRedirectStrategy enhancedRedirectStrategy) {
	    return new CapSessionExpiredStrategy(enhancedRedirectStrategy);
	}

}
