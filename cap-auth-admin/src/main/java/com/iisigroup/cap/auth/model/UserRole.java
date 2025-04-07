/* 
 * RoleSet.java
 *
 * IBM Confidential
 * GBS Source Materials
 * 
 * Copyright (c) 2011 IBM Corp. 
 * All Rights Reserved.
 */
package com.iisigroup.cap.auth.model;

import java.sql.Timestamp;

import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.db.model.listener.CapOidGeneratorListener;
import com.iisigroup.cap.model.GenericBean;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * <pre>
 * 使用者資訊
 * </pre>
 * 
 * @since 2013/12/23
 * @author tammy
 * @version
 *          <ul>
 *          <li>2013/12/23,tammy,new
 *          </ul>
 */

@Entity
@EntityListeners({ CapOidGeneratorListener.class })
@Table(name = "DEF_USERROLE", uniqueConstraints = @UniqueConstraint(columnNames = { "USERCODE", "ROLECODE" }))
public class UserRole extends GenericBean implements DataObject {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, length = 32)
    private String oid;

    @Column(length = 10)
    private String userCode;

    @Id
    @Column(length = 10)
    private String roleCode;

    @Column(length = 10)
    private String updater;

    private Timestamp updateTime;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumns({ @JoinColumn(name = "ROLECODE", referencedColumnName = "CODE", nullable = false, insertable = false, updatable = false) })
    private DefaultRole role;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumns({ @JoinColumn(name = "USERCODE", referencedColumnName = "CODE", nullable = false, insertable = false, updatable = false) })
    private DefaultUser user;

    public DefaultUser getUser() {
        return user;
    }

    public void setUser(DefaultUser user) {
        this.user = user;
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

    public DefaultRole getRole() {
        return role;
    }

    public void setRole(DefaultRole role) {
        this.role = role;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

}
