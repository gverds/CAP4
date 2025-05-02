package com.iisigroup.cap.auth.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.iisigroup.cap.security.model.Role;
import com.iisigroup.cap.security.service.AccessControlService;

import jakarta.servlet.http.HttpServletRequest;

public class CapAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    protected AccessControlService securityService;

    private boolean stripQueryStringFromUrls;
    
    Set<String> ignores;
    
    public CapAuthorizationManager(AccessControlService accessSrv) {
        this.securityService = accessSrv;
    }

    public void setSecurityService(AccessControlService securityService) {
        this.securityService = securityService;
    }

    public boolean isStripQueryStringFromUrls() {
        return stripQueryStringFromUrls;
    }

    public void setStripQueryStringFromUrls(boolean stripQueryStringFromUrls) {
        this.stripQueryStringFromUrls = stripQueryStringFromUrls;
    }

    /**
     * 設置忽略的 Handler
     * 
     * @param ignoreHandlers
     */
    public void setIgnoreHandlers(Set<String> ignoreHandlers) {
        this.ignores = new HashSet<String>();
        ignores.addAll(ignoreHandlers);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        HttpServletRequest request = context.getRequest();

        String url = extractRequestUrl(request);

        // 可選擇忽略的 Handler
        if (ignores.contains(url)) {
            return new AuthorizationDecision(true);
        }

        List<Role> roles = securityService.getAuthRolesByUrl(url);

        if (roles == null || roles.isEmpty()) {
            return new AuthorizationDecision(false);
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (Role role : roles) {
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(role.getCode())) {
                    return new AuthorizationDecision(true);
                }
            }
        }

        return new AuthorizationDecision(false);
    }

    /**
     * Gets the request url.
     * 
     * @param filter
     *            the filter
     * 
     * @return the request url
     */
    protected String extractRequestUrl(HttpServletRequest request) {
        String url = request.getRequestURI();

        int firstQuestionMarkIndex = url.indexOf("?");
        if (stripQueryStringFromUrls) {
        	// Strip anything after a question mark symbol, as per SEC-161. See
            // also SEC-321
            if (firstQuestionMarkIndex != -1) {
                url = url.substring(0, firstQuestionMarkIndex);
            }
        }else {
            if (firstQuestionMarkIndex != -1) {
                String queryString = url.substring(firstQuestionMarkIndex + 1);
                StringBuffer newQueryString = new StringBuffer();
                String[] query = queryString.split("&");
                for (String q : query) {
                    if (q.startsWith("x=") || q.startsWith("jsessionid=")) {
                        continue;
                    } else {
                        newQueryString.append(q).append('&');
                    }
                }
                if (newQueryString.length() > 0) {
                    newQueryString.deleteCharAt(newQueryString.length() - 1);
                }
                return new StringBuffer(url.substring(0, firstQuestionMarkIndex)).append("?").append(newQueryString.toString()).toString();
            	
            }
        }
        if (url.endsWith("/")) {
        	url = url.substring(0, url.length() - 1);
        }
        return url;
    }

}
