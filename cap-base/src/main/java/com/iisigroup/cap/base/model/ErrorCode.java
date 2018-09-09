/*
 * ErrorCode.java
 *
 * Copyright (c) 2011 International Integrated System, Inc.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.base.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;

import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.db.model.listener.CapOidGeneratorListener;
import com.iisigroup.cap.model.GenericBean;
import com.iisigroup.cap.utils.CapString;

/**
 * <pre>
 * 訊息代碼
 * </pre>
 *
 * @since 2011/08/02
 * @author UFO
 * @version
 *          <ul>
 *          <li>2011/08/02,UFO,new
 *          </ul>
 */
@Entity
@EntityListeners({ CapOidGeneratorListener.class })
@Table(name = "CFG_ERRORCODE", uniqueConstraints = @UniqueConstraint(columnNames = { "CODE", "LOCALE" }))
public class ErrorCode extends GenericBean implements DataObject {
    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "|";

    @Id
    @Column(unique = true, nullable = false, length = 32, name = "OID")
    private String oid;

    /**
     * 狀況代碼
     */
    @Column(unique = true, nullable = false, length = 20, name = "CODE")
    private String code;

    /**
     * 語言別
     */
    @Column(nullable = false, length = 5, name = "LOCALE")
    private String locale;

    /**
     * 等級(INFO/ERROR/WARN)
     */
    @Column(length = 5, name = "SEVERITY")
    private String severity;

    /**
     * 狀況說明
     */
    @Column(length = 1024, name = "MESSAGE")
    private String message;

    /**
     * 建議處理方式
     */
    @Column(length = 1024, name = "SUGGESTION")
    private String suggestion;

    /**
     * 系統別
     */
    @Column(length = 5, name = "SYS_ID")
    private String sysId;

    /**
     * 是否送監控
     */
    @Column(length = 1, name = "SEND_MON")
    private String sendMon;

    /**
     * Help URL
     */
    @Column(length = 128, name = "HELP_URL")
    private String helpUrl;

    /**
     * 最後修改人
     */
    @Column(length = 10, name = "UPDATER")
    private String updater;

    /**
     * 最後修改時間
     */
    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    /**
     * get the code
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * set the code
     *
     * @param code
     *            the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * get the locale
     *
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * get the locale
     *
     * @param locale
     *            the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * get the severity
     *
     * @return the severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * set the severity
     *
     * @param severity
     *            the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * get the message
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * set the message
     *
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * get the suggestion
     *
     * @return the suggestion
     */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * set the suggestion
     *
     * @param suggestion
     *            the suggestion to set
     */
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    /*
     * (non-Javadoc)
     *
     * @see tw.com.iisi.cap.model.IDataObject#getOid()
     */
    @Override
    public String getOid() {
        return this.oid;
    }

    /*
     * (non-Javadoc)
     *
     * @see tw.com.iisi.cap.model.IDataObject#setOid(java.lang.String)
     */
    @Override
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * get the sysId
     *
     * @return the sysId
     */
    public String getSysId() {
        return sysId;
    }

    /**
     * @return the sendMon
     */
    public String getSendMon() {
        return sendMon;
    }

    public boolean isSendMon() {
        return "Y".equalsIgnoreCase(StringUtils.trimToEmpty(this.sendMon));
    }

    /**
     * @param sendMon
     *            the sendMon to set
     */
    public void setSendMon(String sendMon) {
        this.sendMon = sendMon;
    }

    /**
     * set the sysId
     *
     * @param sysId
     *            the sysId to set
     */
    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getMonitorHelpURL() {
        String tmpHelpUrl = StringUtils.trimToEmpty(this.getHelpUrl());
        return "".equals(tmpHelpUrl) ? "N/A" : this.getHelpUrl();
    }

    public String getMonitorSeverity() {
        String tmpSeverity = StringUtils.trimToEmpty(this.severity);
        return "".equals(tmpSeverity) || tmpSeverity.length() <= 0 ? "I" : tmpSeverity.substring(0, 1);
    }

    public String getI18nPropString() {
        return CapString.concat(this.severity, SEPARATOR, this.message, SEPARATOR, StringUtils.trimToEmpty(this.suggestion));
    }

    /**
     * @return the helpUrl
     */
    public String getHelpUrl() {
        return helpUrl;
    }

    /**
     * @param helpUrl
     *            the helpUrl to set
     */
    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    /**
     * @return the updater
     */
    public String getUpdater() {
        return updater;
    }

    /**
     * @param updater
     *            the updater to set
     */
    public void setUpdater(String updater) {
        this.updater = updater;
    }

    /**
     * @return the updateTime
     */
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime
     *            the updateTime to set
     */
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

}
