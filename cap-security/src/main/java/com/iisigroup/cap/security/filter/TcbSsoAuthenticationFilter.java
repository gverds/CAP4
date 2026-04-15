/* 
 * TcbSsoAuthenticationFilter.java
 * 
 * Copyright (c) 2020 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.security.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2020年8月20日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2020年8月20日,Lancelot,new
 *          </ul>
 */
public class TcbSsoAuthenticationFilter extends RequestHeaderAuthenticationFilter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object principal = session.getAttribute("principal");
        if (principal == null) {
            try {
                principal = super.getPreAuthenticatedPrincipal(request);
                session.setAttribute("principal", principal);
                //For jmeter壓測,帶header x-workforceid=使用者代號
                session.setAttribute("workforceID", request.getHeader("X-workforceID"));
                session.setAttribute("loginType", "SSO");
            } catch (Exception e) {
                logger.warn("Get principal from request header error.");
            }
        }
        return principal;
    }
}
