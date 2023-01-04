/*
 * Copyright (c) 2009-2012 International Integrated System, Inc.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.web;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iisigroup.cap.component.ErrorResult;
import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.Result;
import com.iisigroup.cap.component.impl.AjaxFormResult;
import com.iisigroup.cap.component.impl.DefaultErrorResult;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.exception.CapFileDownloadException;
import com.iisigroup.cap.exception.CapFlowException;
import com.iisigroup.cap.exception.CapMessageException;
import com.iisigroup.cap.handler.Handler;
import com.iisigroup.cap.operation.simple.SimpleContextHolder;
import com.iisigroup.cap.plugin.HandlerPlugin;
import com.iisigroup.cap.plugin.PluginManager;
import com.iisigroup.cap.utils.CapAppContext;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.CapWebUtil;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * Cap handler Servlet
 * </pre>
 * 
 * @since 2012/9/3
 * @author rodeschen
 * @version
 *          <ul>
 *          <li>2012/9/3,rodeschen,new
 *          <li>2012/9/18,iristu,modify
 *          <li>2013/2/28,rodeschen,add set request
 *          </ul>
 */
@SuppressWarnings("serial")
public class CapHandlerServlet extends HttpServlet {

    protected final Logger logger = LoggerFactory.getLogger(CapHandlerServlet.class);
    public static final String HANDLER = "_handler";
    public static final String ACTION = "_action";
    private static final String FLOWSCHED_KEY = "flowSched-";

    protected String defaultErrorResult;
    protected String defaultRequest;

    protected PluginManager pluginMgr;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String manager = config.getInitParameter("pluginManager");
        pluginMgr = (PluginManager) CapAppContext.getBean(manager);
        defaultRequest = config.getInitParameter("defaultRequest");
        if (CapString.isEmpty(defaultRequest)) {
            defaultRequest = "CapDefaultRequest";
        }
        defaultErrorResult = config.getInitParameter("errorResult");
        if (CapString.isEmpty(defaultErrorResult)) {
            defaultErrorResult = "CapDefaultErrorResult";
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doHandlerAction(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doHandlerAction(req, resp);
    }

    private void addInformationInLogger(HttpServletRequest req, String uuidTx) {
        HttpSession session = req.getSession(false);
        ThreadContext.put("host", req.getLocalAddr());
        ThreadContext.put("uuid", uuidTx);
        // 用戶端IP
        ThreadContext.put("clientAddr", req.getRemoteAddr());
        // Session ID
        ThreadContext.put("sessionId", session.getId());
        ThreadContext.put("reqURI", CapWebUtil.getRequestURL(req));
        // User相關資訊
        String userId = (String) session.getAttribute("LOGIN_USERNAME");
        userId = CapString.isEmpty(userId) ? (String) req.getParameter("j_username") : userId;
        String userName = (String) session.getAttribute("LOGIN_EMPNAME");
        userName = CapString.isEmpty(userName) ? (String) req.getParameter("j_username") : userName;
        if (CapString.isEmpty(userId)) {
            ThreadContext.put("login", "------");
        } else {
            ThreadContext.put("login", userId);
        }
        // User相關資訊
        if (CapString.isEmpty(userName)) {
            // LogContext.setLogin(DEFAULT_LOGIN);
            ThreadContext.put("uname", "------");
        } else {
            // LogContext.setLogin(userId);
            ThreadContext.put("uname", userName);
        }
    }

    protected void doHandlerAction(HttpServletRequest req, HttpServletResponse resp) {
        SimpleContextHolder.resetContext();
        ThreadContext.clearMap();
        String handler = (String) req.getAttribute(HANDLER);
        String action = (String) req.getAttribute(ACTION);
        long st = System.currentTimeMillis();
        String uuidTx = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        addInformationInLogger(req, uuidTx);
        if (logger.isTraceEnabled()) {
            logger.trace("{} Request Data: {}", uuidTx, GsonUtil.objToJson(req.getParameterMap()));
        }
        Object locale = req.getSession().getAttribute(CapWebUtil.localeKey);
        if (locale != null) {
            SimpleContextHolder.put(CapWebUtil.localeKey, locale);
        } else {
            SimpleContextHolder.put(CapWebUtil.localeKey, Locale.getDefault());
        }
        Result result;
        Logger pluginlogger = logger;
        Request request = getDefaultRequest(req);
        try {
            request.setParameter(Handler.FORM_ACTION, action);
            HandlerPlugin plugin = pluginMgr.getPlugin(handler);
            logger.info("{} plugin:{} - {} action: {}", uuidTx, handler, plugin.getClass().getSimpleName(), action);
            plugin.setRequest(request);
            pluginlogger = LoggerFactory.getLogger(plugin.getClass());
            result = plugin.execute(request);
            if (result == null) {
                result = new AjaxFormResult();
            }
        } catch (Exception e) {
        	HttpSession session = req.getSession(false);
        	try {
        		if(e instanceof CapFlowException) {
        			req.getRequestDispatcher("../../page/errorFlow").forward(req, resp);
        		}
        	}catch(Exception ex) {
                logger.error("FlowError redirect to error page exception", ex);
        		
        	}
            ErrorResult errorResult = getDefaultErrorResult();
            if (errorResult == null) {
                result = new DefaultErrorResult(request, e);
            } else {
                errorResult.putError(request, e);
                result = errorResult;
            }
            if (e instanceof CapMessageException) {
                pluginlogger.error(result.getResult());
            } else if (e instanceof CapException && e.getCause() != null) {
                if (e.getCause() instanceof CapFileDownloadException) {
                    try {
                        req.getRequestDispatcher("../../page/error").forward(req, resp);
                    } catch (Exception ex) {
                        logger.error("Download redirect to error page exception", ex);
                    } finally {
                        SimpleContextHolder.resetContext();
                        ThreadContext.clearMap();
                    }
                    return;
                } else {
                    pluginlogger.error(result.getResult(), e.getCause());
                }
            } else {
                pluginlogger.error(result.getResult(), e);
            }
            if(session.getAttribute(FLOWSCHED_KEY + uuidTx) != null) {
            	session.removeAttribute(FLOWSCHED_KEY + uuidTx);
            }
            if (!"true".equals(request.get("iframe"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        result.respondResult(resp);
        logger.debug("{} total spend time : {} ms", uuidTx, (System.currentTimeMillis() - st));
        if (logger.isTraceEnabled()) {
            logger.trace("{} Response Data : {}", uuidTx, result.getLogMessage());
        }
        SimpleContextHolder.resetContext();
        ThreadContext.clearMap();
    }

    protected ErrorResult getDefaultErrorResult() {
        return CapAppContext.getBean(defaultErrorResult);
    }

    protected Request getDefaultRequest(HttpServletRequest req) {
        Request cr = CapAppContext.getBean(defaultRequest);
        cr.setRequestObject(req);
        return cr;
    }

}
