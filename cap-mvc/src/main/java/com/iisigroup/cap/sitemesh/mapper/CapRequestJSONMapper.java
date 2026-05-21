/* 
 * CapRequestJSONMapper.java
 * 
 * Copyright (c) 2009-2013 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.sitemesh.mapper;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.impl.CapSpringMVCRequest;
import com.iisigroup.cap.utils.CapAppContext;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * JavaScript設定request JSON — SiteMesh 2 AbstractDecoratorMapper migrated to Spring HandlerInterceptor.
 * Sets request attribute "reqJSON" with JSON script for the decorator JSP to render.
 * Decorator JSPs: replace &lt;decorator:getProperty property="reqJSON"/&gt;
 *                 with       ${requestScope.reqJSON}
 * </pre>
 * 
 * @since 2013/4/15
 * @author iristu
 * @version
 *          <ul>
 *          <li>2013/4/15,iristu,new
 *          <li>2026/04/21,migration,SiteMesh 3 / Jakarta Servlet: refactored to
 *          HandlerInterceptor
 *          </ul>
 */
public class CapRequestJSONMapper implements HandlerInterceptor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final static String ATTR_KEY = "reqJSON";
    private String ignorePathReg;
    private Set<String> ignoreParams;

    public void setIgnorePathReg(String ignorePathReg) {
        this.ignorePathReg = ignorePathReg;
    }

    public void setDecoratorFile(String decorator) {
        // decoratorFile filtering is not supported in SiteMesh 3 HandlerInterceptor
        // mode;
        // all requests are intercepted regardless of decorator.
    }

    public void setIgnoreParams(String params) {
        if (!CapString.isEmpty(params)) {
            this.ignoreParams = new HashSet<>(Arrays.asList(params.split(",")));
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (ignorePathReg == null || !CapString.checkRegularMatch(UrlUtils.buildRequestUrl(request), ignorePathReg)) {
            Request req = getDefaultRequest();
            req.setRequestObject(request);
            Enumeration<String> fids = request.getParameterNames();
            HashMap<String, String> hm = new HashMap<>();
            while (fids.hasMoreElements()) {
                String field = fids.nextElement();
                if (ignoreParams == null || !ignoreParams.contains(field)) {
                    hm.put(field, req.get(field));
                }
            }
            StringBuffer str = new StringBuffer("<script type=\"text/javascript\">var reqJSON=");
            str.append(GsonUtil.objToJson(hm)).append(";</script>");
            request.setAttribute(ATTR_KEY, str.toString());
        }
        return true;
    }

    private Request getDefaultRequest() {
        Request cr = CapAppContext.getBean("CapDefaultRequest");
        return cr != null ? cr : new CapSpringMVCRequest();
    }
}
