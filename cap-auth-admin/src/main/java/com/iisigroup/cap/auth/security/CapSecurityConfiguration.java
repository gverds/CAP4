package com.iisigroup.cap.auth.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.iisigroup.cap.auth.provider.CapAuthenticationProvider;
import com.iisigroup.cap.auth.service.impl.UserDetailsServiceImpl;
import com.iisigroup.cap.mvc.provider.SwitchProviderFilter;
import com.iisigroup.cap.security.CapSecurityContext;
import com.iisigroup.cap.security.captcha.filter.CaptchaCaptureFilter;
import com.iisigroup.cap.security.filter.CapAnonymousAuthenticationFilter;
import com.iisigroup.cap.security.web.CapAuthenticationEntryPoint;
import com.iisigroup.cap.security.web.CapConcurrentSessionStrategy;
import com.iisigroup.cap.security.web.EnhancedRedirectStrategy;
import com.iisigroup.cap.utils.CapAppContext;

@Configuration
@EnableWebSecurity
@PropertySources({ @PropertySource("classpath:config.properties") })
public class CapSecurityConfiguration {

    @Value("${systemType}")
    private String systemType;

    @Autowired
    private CapAuthenticationEntryPoint capAuthenticationEntryPoint;    
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                            HandlerMappingIntrospector mvcHandlerMappingIntrospector, 
                                            SessionRegistry sessionRegistry) throws Exception {
        
        // Configure switchProviderFilter
//        SwitchProviderFilter switchProviderFilter = new SwitchProviderFilter();
        
        //concurrentSessionFilter.setRedirectStrategy(enhancedRedirectStrategy());

        // Configure CSRF matcher
        RequestMatcher csrfMatcher = new AndRequestMatcher(
            CsrfFilter.DEFAULT_CSRF_MATCHER,
            new NegatedRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/api/**", "POST"),
                new AntPathRequestMatcher("/page/**", "POST"),
                new AntPathRequestMatcher("/**", "POST"))
            )
        );

        http
            .securityContext(securityContext -> securityContext.requireExplicitSave(true)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                		new AntPathRequestMatcher("/img/**"),
                		new AntPathRequestMatcher("/**/images/**"),
                		new AntPathRequestMatcher("/**/fonts/**"),
                		new AntPathRequestMatcher("/jquery/**"),
                		new AntPathRequestMatcher("/**/**.css"),
                		new AntPathRequestMatcher("/**/**.js"),
                		new AntPathRequestMatcher("/**/**.map"),
                		new AntPathRequestMatcher("/i18njs*"),
                		new AntPathRequestMatcher("/captcha.png*"),
                		new AntPathRequestMatcher("/app/error/message"),
                		new AntPathRequestMatcher("/page/login*"),
                		new AntPathRequestMatcher("/page/error*"),
                		new AntPathRequestMatcher("/page/home/*")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/page/index*"),
                		new AntPathRequestMatcher("/page/common/*"), 
                		new AntPathRequestMatcher("/page/**"),
                		new AntPathRequestMatcher("/**")).authenticated()
                .anyRequest().access(authorizationManager())
            )
            .exceptionHandling((exceptions) -> exceptions
                .accessDeniedPage("/page/login?error")
                .authenticationEntryPoint(capAuthenticationEntryPoint)
            )
            .anonymous(anonymous -> anonymous
                .disable()
            )
            //.addFilterBefore(securityContextPersistenceFilter, SecurityContextPersistenceFilter.class)
            .addFilterAt(concurrentSessionFilter(), ConcurrentSessionFilter.class)
            .addFilterAt(switchProviderFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(anonymousAuthFilter(), AnonymousAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions
                    .sameOrigin()
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("script-src 'self' 'unsafe-inline' 'unsafe-eval' https://maps.googleapis.com https://ajax.googleapis.com;")
                )
            )
            .logout(logout -> logout
                .logoutUrl("/j_spring_security_logout")
                .logoutSuccessUrl("/page/login")
            )
            .sessionManagement(session -> session
                .sessionAuthenticationStrategy(compositeSessionAuthenticationStrategy())
            )
            .csrf(csrf -> csrf
                .requireCsrfProtectionMatcher(csrfMatcher)
            );

        // Set non-expression based access decision manager
        //http.setSharedObject(AccessDecisionManager.class, accessDecisionManager());
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(customAuthenticationProvider());
    }

    @Bean
    public ConcurrentSessionFilter concurrentSessionFilter() {
    	ConcurrentSessionFilter concurrentSessionFilter = new ConcurrentSessionFilter(
                sessionRegistry(), 
                "/j_spring_security_logout"
            );
    	concurrentSessionFilter.setRedirectStrategy(enhancedRedirectStrategy());
    	return concurrentSessionFilter;
    }
    
    @Bean
    public CapAnonymousAuthenticationFilter anonymousAuthFilter() {
    	CapAnonymousAuthenticationFilter anonymousAuthFilter = new CapAnonymousAuthenticationFilter("cap");
    	return anonymousAuthFilter;
    }

    @Bean
    public CapAuthenticationProvider customAuthenticationProvider() {
    	CapAuthenticationProvider capAuthenticationProvider = new CapAuthenticationProvider();
    	capAuthenticationProvider.setAccessControlService(CapAppContext.getBean("accessControlService"));
    	capAuthenticationProvider.setPasswordService(CapAppContext.getBean("passwordService"));
    	capAuthenticationProvider.setUserService(userDetailsServiceImpl());
    	capAuthenticationProvider.setCaptchaCaptureFilter(captchaCaptureFilter());
    	return capAuthenticationProvider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public EnhancedRedirectStrategy enhancedRedirectStrategy() {
        return new EnhancedRedirectStrategy();
    }

    @Bean
    public SwitchProviderFilter switchProviderFilter() {
    	SwitchProviderFilter switchProviderFilter = new SwitchProviderFilter();
    	switchProviderFilter.setAuthenticationManager(authenticationManager());
    	switchProviderFilter.setAuthenticationFailureHandler(CapAppContext.getBean("ajaxAuthenticationFailureHandler"));
    	switchProviderFilter.setAuthenticationSuccessHandler(CapAppContext.getBean("ajaxAuthenticationSuccessHandler"));
    	switchProviderFilter.setFilterProcessesUrl("/j_spring_security_check");
        return switchProviderFilter;
    }

    @Bean
    public CaptchaCaptureFilter captchaCaptureFilter() {
        return new CaptchaCaptureFilter();
    }

    @Bean
    public UserDetailsService userDetailsServiceImpl() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public CapConcurrentSessionStrategy concurrentSessionStrategy(SessionRegistry sessionRegistry) {
        CapConcurrentSessionStrategy strategy = new CapConcurrentSessionStrategy(sessionRegistry);
        strategy.setMaximumSessions(1);
        strategy.setExceptionIfMaximumExceeded(false);
        return strategy;
    }

    @Bean
    public SessionAuthenticationStrategy compositeSessionAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(Arrays.asList(
            concurrentSessionStrategy(sessionRegistry()),
            new SessionFixationProtectionStrategy(),
            new RegisterSessionAuthenticationStrategy(sessionRegistry())
        ));
    }

    @Bean
    public CapAuthenticationEntryPoint capAuthenticationEntryPoint() {
        CapAuthenticationEntryPoint entryPoint = new CapAuthenticationEntryPoint("/page/login");
        entryPoint.setForceHttps(false);
        return entryPoint;
    }

//    @Bean
//    public AccessDecisionManager accessDecisionManager() {
//        List<AccessDecisionVoter<?>> voters = Arrays.asList(
//            capPermissionVoter(),
//            new AuthenticatedVoter()
//        );
//        AffirmativeBased accessDecisionManager = new AffirmativeBased(voters);
//        accessDecisionManager.setAllowIfAllAbstainDecisions(false);
//        return accessDecisionManager;
//    }

    @Bean
    public CapPermissionVoter capPermissionVoter() {
        CapPermissionVoter voter = new com.iisigroup.cap.auth.security.CapPermissionVoter(CapAppContext.getBean("accessControlService"));
        voter.setRolePrefix(systemType);
        voter.setSecurityService(CapAppContext.getBean("accessControlService"));
        voter.setStripQueryStringFromUrls(true);
        return voter;
    }

    @Bean
    public LoggerListener loggerListener() {
        return new LoggerListener();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    @Scope(scopeName = "session")
    public CapSecurityContext capSecurityContext() {
        return new CapSecurityContext();
    }
    
    @Bean
    public AuthorizationManager<RequestAuthorizationContext> authorizationManager() {
        return new AuthorizationManager<RequestAuthorizationContext>() {
            @Override
            public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
                List<CustomDecisionVoter<?>> voters = new ArrayList<CustomDecisionVoter<?>>();

                CapPermissionVoter capPermissionVoter = capPermissionVoter();
                capPermissionVoter.setRolePrefix("ROLE_");

                capPermissionVoter.setIgnoreHandlers(new HashSet<>(
                		Arrays.asList(
                				"i18nhandler", 
                				"codetypehandler"
                		)
                ));
                voters.add(capPermissionVoter);

                List<ConfigAttribute> rolePrefix = new ArrayList<>();
                rolePrefix.add(new SecurityConfig("ROLE_"));
                int deny = 0;
                for (CustomDecisionVoter voter : voters) {
                    int result = voter.vote(authentication.get(), object, rolePrefix);
                    switch (result) {
                    case CustomDecisionVoter.ACCESS_GRANTED:
                        return new AuthorizationDecision(true);
                    case CustomDecisionVoter.ACCESS_DENIED:
                        deny++;
                        break;
                    default:
                        break;
                    }
                }
                if (deny > 0) {
                    throw new AccessDeniedException("Access is denied");
                }
                return new AuthorizationDecision(false);
            }
        };
    }
}