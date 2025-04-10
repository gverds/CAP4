package com.iisigroup.cap.auth.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.iisigroup.cap.utils.CapAppContext;

//@Configuration
public class CapSecurityConfig {

//    @Bean
    public AuthorizationManager<RequestAuthorizationContext> authorizationManager() {
        return new AuthorizationManager<RequestAuthorizationContext>() {
            @Override
            public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
                List<CustomDecisionVoter<?>> voters = new ArrayList<CustomDecisionVoter<?>>();

                CapPermissionVoter capPermissionVoter = new CapPermissionVoter(CapAppContext.getBean("accessControlService"));
                capPermissionVoter.setRolePrefix("EL");
                capPermissionVoter.setIgnoreHandlers(new HashSet<>(Arrays.asList("i18nhandler", "codetypehandler", "Simplefileuploadhandler", "simplefiledwnhandler")));
                voters.add(capPermissionVoter);

                List<ConfigAttribute> rolePrefix = new ArrayList<>();
                rolePrefix.add(new SecurityConfig("EL"));
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