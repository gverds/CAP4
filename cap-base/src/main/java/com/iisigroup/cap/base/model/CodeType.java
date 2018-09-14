/*
 * CodeType.java
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

import com.iisigroup.cap.db.annotation.Table;
import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.model.GenericBean;

/**
 * <pre>
 * CodeType
 * </pre>
 * 
 * @since 2012/9/20
 * @author iristu
 * @version
 *          <ul>
 *          <li>2012/9/20,iristu,new
 *          </ul>
 */
@Table(name = "CFG_CODETYPE")
public class CodeType extends GenericBean implements DataObject {

    private static final long serialVersionUID = 7640958465353039790L;

    private String oid;

    private String locale;

    private String codeType;

    private String codeValue;

    private String codeDesc;

    private Integer codeOrder;

    private String updater;

    private Timestamp updateTime;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public String getCodeDesc() {
        return codeDesc;
    }

    public void setCodeDesc(String codeDesc) {
        this.codeDesc = codeDesc;
    }

    public Integer getCodeOrder() {
        return codeOrder;
    }

    public void setCodeOrder(Integer codeOrder) {
        this.codeOrder = codeOrder;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

}
