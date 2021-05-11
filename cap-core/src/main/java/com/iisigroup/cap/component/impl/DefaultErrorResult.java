/*
 * Copyright (c) 2009-2011 International Integrated System, Inc.
 * 11F, No.133, Sec.4, Minsheng E. Rd., Taipei, 10574, Taiwan, R.O.C.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System,Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. ("Confidential Information").
 */
package com.iisigroup.cap.component.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletResponse;

import org.apache.commons.lang3.CharEncoding;

import com.iisigroup.cap.component.ErrorResult;
import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.Result;
import com.iisigroup.cap.exception.CapClosePageException;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.exception.CapMessageException;
import com.iisigroup.cap.exception.CapSessioniExpireException;
import com.iisigroup.cap.operation.simple.SimpleContextHolder;
import com.iisigroup.cap.utils.CapAppContext;
import com.iisigroup.cap.utils.CapDate;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.CapWebUtil;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * 錯誤訊息回應
 * </pre>
 * 
 * @since 2010/11/24
 * @author iristu
 * @version
 *          <ul>
 *          <li>iristu,2010/11/24,new
 *          <li>RodesChen,2011/6/2,增加關閉畫面錯誤
 *          <li>2011/11/1,rodeschen,from cap
 *          </ul>
 */
public class DefaultErrorResult implements ErrorResult {

    private static final long serialVersionUID = 1L;
    public static final String AJAX_HANDLER_EXCEPTION = "AJAX_HANDLER_EXCEPTION";
    public static final String AJAX_MESSAGE_HANDLER_EXCEPTION = "AJAX_MESSAGE_HANDLER_EXCEPTION";
    public static final String AJAX_SESSION_EXPIRE_EXCEPTION = "AJAX_SESSION_EXPIRE_EXCEPTION";
    /** 關閉畫面錯誤類別 */
    public static final String AJAX_CLOSE_PAGE_HANDLER_EXCEPTION = "AJAX_CLOSE_PAGE_HANDLER_EXCEPTION";

    Map<String, Object> errorMessage = new HashMap<String, Object>();

    String logMessage = "";
    private String contentType;
    private String encoding;
    private static final String READABLE_ERROR_MSG = "例外錯誤，請洽系統人員。";

    public DefaultErrorResult() {
    }

    public DefaultErrorResult(Request request, Exception e) {
        this.putError(request, e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see tw.com.iisi.cap.response.IResult#getResult()
     */
    @Override
    public String getResult() {
        return GsonUtil.mapToJson(errorMessage);
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void putError(Request request, Exception e) {
        if (e instanceof CapMessageException) {
            CapMessageException ce = (CapMessageException) e;
            logMessage = ce.getMessage();
            if (!CapString.isEmpty(ce.getMessageKey())) {
                logMessage = ce.getMessageKey();
            }
            logMessage = formatMessage(request, logMessage, ce.getExtraInformation());
            errorMessage.put(AJAX_MESSAGE_HANDLER_EXCEPTION, "[" + CapDate.getCurrentTimestamp() + "] " + logMessage);
        } else if (e instanceof CapClosePageException) {
            CapClosePageException ce = (CapClosePageException) e;
            logMessage = ce.getMessage();
            if (!CapString.isEmpty(ce.getMessageKey())) {
                logMessage = ce.getMessageKey();
            }
            logMessage = formatMessage(request, logMessage, ce.getExtraInformation());
            errorMessage.put(AJAX_CLOSE_PAGE_HANDLER_EXCEPTION, "[" + CapDate.getCurrentTimestamp() + "] " + logMessage);
        } else if (e instanceof CapSessioniExpireException) {
            CapSessioniExpireException ce = (CapSessioniExpireException) e;
            logMessage = ce.getMessage();
            if (!CapString.isEmpty(ce.getMessageKey())) {
                logMessage = ce.getMessageKey();
            }
            logMessage = formatMessage(request, logMessage, ce.getExtraInformation());
            errorMessage.put(AJAX_SESSION_EXPIRE_EXCEPTION, "[" + CapDate.getCurrentTimestamp() + "] " + logMessage);
        } else if (e instanceof CapException) {
            CapException ce = (CapException) e;
            logMessage = new StringBuffer(ce.getCauseClass().getName()).append(":").append(e.getMessage()).toString();
            errorMessage.put(AJAX_HANDLER_EXCEPTION, "[" + CapDate.getCurrentTimestamp() + "] " + READABLE_ERROR_MSG);
        } else {
            logMessage = e.getLocalizedMessage();
            errorMessage.put(AJAX_HANDLER_EXCEPTION, "[" + CapDate.getCurrentTimestamp() + "] " + READABLE_ERROR_MSG);
        }
    }

    @Override
    public void add(Result result) {
        Map<String, Object> map = GsonUtil.jsonToMap(result.getResult());
        this.errorMessage.putAll(map);
        this.logMessage = result.getLogMessage();
    }

    @Override
    public String getContextType() {
        if (contentType != null) {
            return this.contentType;
        } else {
            return "text/plain";
        }
    }

    @Override
    public String getEncoding() {
        if (encoding != null) {
            return this.encoding;
        } else {
            return CharEncoding.UTF_8;
        }
    }

    @Override
    public void setContextType(String cxtType) {
        this.contentType = cxtType;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public void respondResult(ServletResponse response) {
        new StringResponse(getContextType(), getEncoding(), getResult()).respond(response);
    }

    public Locale getLocale(Request request) {
        return (Locale) SimpleContextHolder.get(CapWebUtil.localeKey);

    }

    /**
     * 格式化訊息
     * 
     * @param component
     *            {@link org.apache.wicket.Component}
     * @param msgKey
     *            錯誤訊息
     * @param extraInfo
     *            其它資訊
     * @return 錯誤訊息
     */
    protected String formatMessage(Request request, String msgKey, Object extraInfo) {
        Locale locale = getLocale(request);
        if (extraInfo != null) {
            return CapAppContext.getMessage(msgKey, (Object[]) extraInfo, locale);
        } else {
            return CapAppContext.getMessage(msgKey, locale);
        }

    }

}
