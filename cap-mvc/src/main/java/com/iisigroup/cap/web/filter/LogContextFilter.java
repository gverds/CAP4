/*
 * LogContextFilter.java
 *
 * Copyright (c) 2009-2011 International Integrated System, Inc.
 * 11F, No.133, Sec.4, Minsheng E. Rd., Taipei, 10574, Taiwan, R.O.C.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System,Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. ("Confidential Information").
 */
package com.iisigroup.cap.web.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.CapWebUtil;

/**
 * <p>
 * set log4j MDC for log user information.
 * </p>
 * 
 * @author iris tu
 * @version
 *          <ul>
 *          <li>2011-11-23,iristu,new
 *          <li>2013-1-23,RodesChen,move getRequestURL to CapWebUtil
 *          <li>2013-6-3,RodesChen,增加 UUID 取代 threadID 以防值過長(Weblogic)
 *          <li>2020-4-8,Sunkist,Update for log4j2
 *          </ul>
 */
public class LogContextFilter implements Filter {
    public final static String LOGIN_USERNAME = "LOGIN_USERNAME";
    public final static String DEFAULT_LOGIN = "------";

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // LogContext.resetLogContext();
        ThreadContext.clearMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        // Host IP
        // LogContext.setHost(req.getLocalAddr());
        // LogContext.setUUID(CapString.getUUIDString());
        ThreadContext.put("host", req.getLocalAddr());
        ThreadContext.put("uuid", CapString.getUUIDString());
        if (session == null) {
            // LogContext.setLogin(DEFAULT_LOGIN);
            ThreadContext.put("login", DEFAULT_LOGIN);
        } else {
            // 用戶端IP
            // LogContext.setClientAddr(req.getRemoteAddr());
            ThreadContext.put("clientAddr", req.getRemoteAddr());
            // Session ID
            // LogContext.setSessionId(session.getId());
            // LogContext.setRequestURL(CapWebUtil.getRequestURL(req));
            ThreadContext.put("sessionId", session.getId());
            ThreadContext.put("reqURI", CapWebUtil.getRequestURL(req));
            // User相關資訊
            String userId = (String) session.getAttribute(LOGIN_USERNAME);
            userId = CapString.isEmpty(userId) ? (String) request.getParameter("j_username") : userId;
            if (CapString.isEmpty(userId)) {
                // LogContext.setLogin(DEFAULT_LOGIN);
                ThreadContext.put("login", DEFAULT_LOGIN);
            } else {
                // LogContext.setLogin(userId);
                ThreadContext.put("login", userId);
            }
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Content-Security-Policy", "script-src 'self' https://maps.googleapis.com 'unsafe-inline' 'unsafe-eval'");
        chain.doFilter(request, response);
        // LogContext.resetLogContext();
        ThreadContext.clearMap();
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }
}

class LogContext extends InheritableThreadLocal<Map<String, String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogContext.class);
    private static ThreadLocal<Map<String, String>> logContext = new InheritableThreadLocal<>();
    private static boolean useMDC = false;
    public static final String LOGIN = "login";
    public static final String UUID = "uuid";
    public static final String SESSION_ID = "sessionId";
    public static final String HOST = "host";
    public static final String CLIENT_ADDR = "clientAddr";
    public static final String REQUEST_URI = "reqURI";
    static {
        try {
            Class.forName("org.apache.logging.log4j.ThreadContext");
            useMDC = true;
        } catch (Throwable t) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("org.apache.logging.log4j.ThreadContext was not found on the classpath, continue without");
            }
        }
    }

    /**
     * Get a map containing all the objects held by the current thread.
     */
    private static Map<String, String> getContext() {
        if (useMDC) {
            return ThreadContext.getContext();
        } else {
            Map<String, String> m = logContext.get();
            if (m == null) {
                m = new LinkedHashMap<>();
                logContext.set(m);
            }
            return m;
        }
    }

    /**
     * Get the context identified by the key parameter.
     * 
     * @param key
     *            the key
     * @return Object
     */
    public static Object get(String key) {
        if (useMDC) {
            return ThreadContext.get(key);
        } else {
            return getContext().get(key);
        }
    }

    /**
     * Put a context value (the o parameter) as identified with the key parameter into the current thread's context map.
     * 
     * @param key
     *            the Key
     * @param o
     *            Object
     */
    public static void put(String key, String o) {
        if (useMDC) {
            ThreadContext.put(key, CapString.trimNull(o));
        } else {
            getContext().put(key, o);
        }
    }

    /**
     * Remove the the context identified by the key parameter.
     * 
     * @param key
     *            the Key
     */
    public static void remove(String key) {
        if (useMDC) {
            ThreadContext.remove(key);
        } else {
            getContext().remove(key);
        }
    }

    /**
     * Remove all the object put in this thread context.
     */
    public static void resetLogContext() {
        if (useMDC) {
            ThreadContext.clearMap();
        } else {
            if (getContext() != null) {
                getContext().clear();
            }
        }
    }

    /**
     * Only used if jdk logging is used.
     * 
     * @return String
     */
    public static String toLogPrefixString() {
        Map<String, String> m = getContext();
        Iterator<Entry<String, String>> i = m.entrySet().iterator();
        StringBuilder sb = new StringBuilder("[");
        while (i.hasNext()) {
            Entry<String, String> e = i.next();
            sb.append(e.getKey()).append("=").append(e.getValue());
            if (i.hasNext()) {
                sb.append("&");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * set the given login in the map
     * 
     * @param login
     *            the user Id
     */
    public static void setLogin(String login) {
        put(LOGIN, login);
    }

    /**
     * set the given IP in the map
     * 
     * @param host
     *            the host
     */
    public static void setHost(String host) {
        put(HOST, host);
    }

    /**
     * set the given web session in the map
     * 
     * @param sessionId
     *            the session id
     */
    public static void setSessionId(String sessionId) {
        put(SESSION_ID, sessionId);
    }

    public static void setClientAddr(String addr) {
        put(CLIENT_ADDR, addr);
    }

    public static void setRequestURL(String url) {
        put(REQUEST_URI, url);
    }

    public static void setUUID(String uuid) {
        put(UUID, uuid);
    }
}