/* 
 * CapConcurrentSessionStrategy.java
 * 
 * Copyright (c) 2020 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.security.web;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.util.Assert;

import com.iisigroup.cap.security.CapSecurityContext;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2020年10月7日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2020年10月7日,Lancelot,new
 *          </ul>
 */
public class CapConcurrentSessionStrategy extends ConcurrentSessionControlAuthenticationStrategy {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final SessionRegistry sessionRegistry;

    /**
     * @param sessionRegistry
     */
    public CapConcurrentSessionStrategy(SessionRegistry sessionRegistry) {
        super(sessionRegistry);
        Assert.notNull(sessionRegistry, "The sessionRegistry cannot be null");
        this.sessionRegistry = sessionRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy#onAuthentication(org.springframework.security.core.Authentication,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        final List<SessionInformation> sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), false);
        int sessionCount = sessions.size();
        int allowedSessions = getMaximumSessionsForThisUser(authentication);
        HttpSession session = request.getSession(false);
        String forceLogout = request.getParameter("forceLogout");

        String currentId = CapSecurityContext.getUserId();
        String requestId = request.getParameter("j_username");
        if (!StringUtils.isBlank(currentId) && !"SYS".equalsIgnoreCase(currentId) && !currentId.equalsIgnoreCase(requestId)) {
            // 已經登入其他使用者
            if (StringUtils.isBlank(forceLogout)) {
                try {
                    response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, currentId);
                    throw new SessionAuthenticationException("error");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    logger.error(e.toString());
                }
            } else {
                final List<SessionInformation> currentSessions = sessionRegistry.getAllSessions(CapSecurityContext.getUser(), false);
                allowableSessionsExceeded(currentSessions, allowedSessions, sessionRegistry);
            }
        }
        if (sessionCount < allowedSessions) {
            // They haven't got too many login sessions running at present
            return;
        }

        if (allowedSessions == -1) {
            // We permit unlimited logins
            return;
        }

        if (sessionCount == allowedSessions) {
            if (session != null) {
                // Only permit it though if this request is associated with one of the
                // already registered sessions
                for (SessionInformation si : sessions) {
                    if (si.getSessionId().equals(session.getId())) {
                        return;
                    }
                }
            }
            // If the session is null, a new one will be created by the parent class,
            // exceeding the allowed number
        }
        if (StringUtils.isBlank(forceLogout)) {
            try {
                session.invalidate();
                response.sendError(HttpServletResponse.SC_CONFLICT);
                throw new SessionAuthenticationException("error");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                logger.error(e.toString());
            }
        } else if ("true".equalsIgnoreCase(forceLogout)) {
            allowableSessionsExceeded(sessions, allowedSessions, sessionRegistry);
        }
    }

    @Override
    protected void allowableSessionsExceeded(List<SessionInformation> sessions, int allowableSessions, SessionRegistry registry) throws SessionAuthenticationException {
        for (SessionInformation session : sessions) {
            session.expireNow();
        }
    }

}
