/*
 * Reminds.java
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

import java.math.BigDecimal;

import com.iisigroup.cap.db.annotation.Table;
import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.model.GenericBean;

/**
 * <p>
 * 通知方式檔.
 * </p>
 * 
 * @author tammy
 * @version
 *          <ul>
 *          <li>2014/1/27,tammy,new
 *          </ul>
 */
@Table(name = "CFG_REMINDS")
public class Reminds extends GenericBean implements DataObject {

    private static final long serialVersionUID = -6589065415398449822L;

    /** id */
    private String oid;
    
    /** pid */
    private String pid;
    
    /** 對象號碼 */
    private String scopePid;
    
    /** 提醒方式 */
    private String styleTyp;
    
    /** 顏色 */
    private String styleClr;
    
    /** 數值 */
    private BigDecimal style;
    
    /** 單位 */
    private BigDecimal unit;
    
    /** 是否完成 */
    private String ynFlag;

    private Remind remind;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getScopePid() {
        return scopePid;
    }

    public void setScopePid(String scopePid) {
        this.scopePid = scopePid;
    }

    public String getStyleTyp() {
        return styleTyp;
    }

    public void setStyleTyp(String styleTyp) {
        this.styleTyp = styleTyp;
    }

    public String getStyleClr() {
        return styleClr;
    }

    public void setStyleClr(String styleClr) {
        this.styleClr = styleClr;
    }

    public BigDecimal getStyle() {
        return style;
    }

    public void setStyle(BigDecimal style) {
        this.style = style;
    }

    public BigDecimal getUnit() {
        return unit;
    }

    public void setUnit(BigDecimal unit) {
        this.unit = unit;
    }

    public String getYnFlag() {
        return ynFlag;
    }

    public void setYnFlag(String ynFlag) {
        this.ynFlag = ynFlag;
    }

    public Remind getRemind() {
        return remind;
    }

    public void setRemind(Remind remind) {
        this.remind = remind;
    }

}
