/* 
 * AuditLog.java
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

/**<pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * @since  2018年7月27日
 * @author Lancelot
 * @version <ul>
 *           <li>2018年7月27日,Lancelot,new
 *          </ul>
 */
/**
 * <pre>
 * 使用軌跡 persistence model.
 * </pre>
 * 
 * @since 2014/1/16
 * @author Sunkist Wang
 * @version
 *          <ul>
 *          <li>2014/1/16,Sunkist Wang,new
 *          </ul>
 */
@Table(name = "AUDIT_LOG")
public class AuditLog extends GenericBean implements DataObject {

    private static final long serialVersionUID = 4443971927284313493L;

    /** unique id */
    private String oid;

    /** 使用者SSOID */
    private String userCode;

    /** IP 位址 */
    private String ipAddress;

    /** 作業代號 */
    private String functionId;

    /** 新增/修改/刪除/查詢/匯出/匯入 */
    private String actionType;

    /** 執行時間 */
    private Timestamp executeDate;

    /** 備註/Key值 */
    private String remark;

    private String systype;

    public String getId() {
        return oid;
    }

    public String getSystype() {
        return systype;
    }

    public void setSystype(String systype) {
        this.systype = systype;
    }

    public void setId(String id) {
        this.oid = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Timestamp getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(Timestamp executeDate) {
        this.executeDate = executeDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return the userCode
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * @param userCode
     *            the userCode to set
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

}
