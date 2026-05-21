/* 
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.sitemesh.mapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.iisigroup.cap.mvc.i18n.MessageBundleScriptCreator;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.CapWebUtil;

/**
 * <pre>
 * i18n Decorator — SiteMesh 2 AbstractDecoratorMapper migrated to Spring HandlerInterceptor.
 * Sets request attribute "i18n" with the i18n script for the decorator JSP to render.
 * Decorator JSPs: replace &lt;decorator:getProperty property="i18n"/&gt;
 *                 with       ${requestScope.i18n}
 * </pre>
 * 
 * @since 2012/9/28
 * @author rodeschen
 * @version
 *          <ul>
 *          <li>2012/9/28,rodeschen,new
 *          <li>2013/1/23,RodesChen,fix weblogic getPath error
 *          <li>2026/04/21,migration,SiteMesh 3 / Jakarta Servlet: refactored to
 *          HandlerInterceptor
 *          </ul>
 */
public class CapI18nMapper implements HandlerInterceptor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final static String ATTR_I18N = "i18n";
    private String ignorePathReg;

    public void setIgnorePathReg(String ignorePathReg) {
        this.ignorePathReg = ignorePathReg;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String pathInfo = CapWebUtil.getRequestURL(request);
        if (!CapString.checkRegularMatch(UrlUtils.buildRequestUrl(request), ignorePathReg)) {
            request.setAttribute(ATTR_I18N,
                    MessageBundleScriptCreator.createScript(
                            pathInfo.replaceAll("(^/page/|[.][jJ][sS][pP]$)", "")));
        }
        return true;
    }
}
