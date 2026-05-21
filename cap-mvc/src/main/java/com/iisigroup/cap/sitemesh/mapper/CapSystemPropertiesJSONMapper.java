/* 
 * CCSystemPropJSONMapper.java
 * 
 * Copyright (c) 2009-2014 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.sitemesh.mapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import com.iisigroup.cap.utils.CapAppContext;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * 實作 Sitemesh 的 JSONMapper，把系統參數帶到 page。
 * Migrated from SiteMesh 2 AbstractDecoratorMapper to Spring HandlerInterceptor.
 * Sets request attribute "prop" with system properties JSON for the decorator JSP to render.
 * Decorator JSPs: replace &lt;decorator:getProperty property="prop"/&gt;
 *                 with       ${requestScope.prop}
 * </pre>
 * 
 * @since 2014/1/19
 * @author Sunkist Wang
 * @version
 *          <ul>
 *          <li>2014/1/19,Sunkist Wang,new
 *          <li>2026/04/21,migration,SiteMesh 3 / Jakarta Servlet: refactored to
 *          HandlerInterceptor
 *          </ul>
 */
public class CapSystemPropertiesJSONMapper implements HandlerInterceptor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final static String ATTR_KEY = "prop";
    private Set<String> searchKeys;
    private Map<String, Object> sysProp;

    public void setDecoratorFile(String decorator) {
        // decoratorFile filtering is not supported in SiteMesh 3 HandlerInterceptor
        // mode;
        // all requests are intercepted regardless of decorator.
    }

    public void setSearchKeys(String params) {
        if (!CapString.isEmpty(params)) {
            this.searchKeys = new HashSet<>(Arrays.asList(params.split(",")));
        }
    }

    private Map<String, Object> getSysProp() {
        if (sysProp == null) {
            sysProp = CapAppContext.getBean("sysProp");
        }
        return sysProp;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, Object> hm = new HashMap<>();
        Map<String, Object> props = getSysProp();
        if (searchKeys != null && props != null) {
            for (String sKey : searchKeys) {
                String val = CapString.trimNull(props.get(sKey));
                Pattern pattern = Pattern.compile("(^true$|^false$)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(val);
                if (matcher.matches()) {
                    hm.put(sKey, Boolean.valueOf(val));
                } else {
                    hm.put(sKey, val);
                }
            }
        }
        StringBuffer str = new StringBuffer("<script type=\"text/javascript\">var prop=");
        str.append(GsonUtil.mapToJson(hm)).append(";</script>");
        request.setAttribute(ATTR_KEY, str.toString());
        return true;
    }
}
