package com.iisigroup.cap.mvc.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.db.model.listener.CapOidGeneratorListener;
import com.iisigroup.cap.model.GenericBean;
import com.iisigroup.cap.operation.simple.SimpleContextHolder;
import com.iisigroup.cap.security.model.Role;
import com.iisigroup.cap.security.model.User;
import com.iisigroup.cap.utils.CapWebUtil;

/**
 * <pre>
 * 人員基本資料
 * </pre>
 * @since  2021年5月25日
 * @author
 * @version <ul>
 *           <li>2021年5月25日,new
 *          </ul>
 */
@Entity
@EntityListeners({ CapOidGeneratorListener.class })
@Table(name = "DEF_USER", uniqueConstraints = @UniqueConstraint(columnNames = { "oid" }))
public class DefUser extends GenericBean implements DataObject, User {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "oid",length = 32, columnDefinition = "CHAR(32)", nullable = false)
    private String oid;

    /** *人員代碼 */
    @Column(name = "empId",length = 10, columnDefinition = "VARCHAR(10)")
    private String empId;

    /** *人員名稱 */
    @Column(name = "empName",length = 30, columnDefinition = "VARCHAR(30)")
    private String empName;

    /** *所屬單位代碼 */
    @Column(name = "deptId",length = 10, columnDefinition = "VARCHAR(10)")
    private String deptId;

    /** *職章檔案名稱 */
    @Column(name = "empPicName",length = 100, columnDefinition = "VARCHAR(100)")
    private String empPicName;
    
    /** *職章 */
    @Column(name = "empPic", columnDefinition = "BLOB")
    private byte[] empPic;

    /** *啟用註記 */
    @Column(name = "enableFlg",length = 1, columnDefinition = "VARCHAR(1)")
    private String enableFlg;

    /** *新增人員 */
    @Column(name = "creUserId",length = 10, columnDefinition = "VARCHAR(10)")
    private String creUserId;

    /** *異動人員角色註記 */
    @Column(name = "supFlg",length = 1, columnDefinition = "VARCHAR(1)")
    private String supFlg;

    /** *電話 */
    @Column(name = "telNo",length = 20, columnDefinition = "VARCHAR(20)")
    private String telNo;

    /** *分機 */
    @Column(name = "telExt",length = 10, columnDefinition = "VARCHAR(10)")
    private String telExt;
    
    /** *新增人員名稱 */
    @Column(name = "creUserName",length = 60, columnDefinition = "VARCHAR(60)")
    private String creUserName;

    /** *新增日期 */
    @Column(name = "creDate", columnDefinition = "TIMESTAMP")
    private Timestamp creDate;

    /** *更新人員 */
    @Column(name = "updUserId",length = 10, columnDefinition = "VARCHAR(10)")
    private String updUserId;

    /** *更新人員名稱 */
    @Column(name = "updUserName",length = 60, columnDefinition = "VARCHAR(60)")
    private String updUserName;

    /** *更新日期 */
    @Column(name = "updDate", columnDefinition = "TIMESTAMP")
    private Timestamp updDate;

    @Transient
    private String deptName;

    @Transient
    private String roleId;
    /**
     * get oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * set oid
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * get 人員代碼
     */
    public String getEmpId() {
        return empId;
    }

    /**
     * set 人員代碼
     */
    public void setEmpId(String empId) {
        this.empId = empId;
    }

    /**
     * get 人員名稱
     */
    public String getEmpName() {
        return empName;
    }

    /**
     * set 人員名稱
     */
    public void setEmpName(String empName) {
        this.empName = empName;
    }

    /**
     * get 所屬單位代碼
     */
    public String getDeptId() {
        return deptId;
    }

    /**
     * set 所屬單位代碼
     */
    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    /**
     * get 職章
     */
    public byte[] getEmpPic() {
        return empPic;
    }

    /**
     * set 職章
     */
    public void setEmpPic(byte[] empPic) {
        this.empPic = empPic;
    }

    /**
     * get 新增人員
     */
    public String getCreUserId() {
        return creUserId;
    }

    /**
     * set 新增人員
     */
    public void setCreUserId(String creUserId) {
        this.creUserId = creUserId;
    }

    /**
     * get 新增人員名稱
     */
    public String getCreUserName() {
        return creUserName;
    }

    /**
     * set 新增人員名稱
     */
    public void setCreUserName(String creUserName) {
        this.creUserName = creUserName;
    }

    /**
     * get 新增日期
     */
    public Timestamp getCreDate() {
        return creDate;
    }

    /**
     * set 新增日期
     */
    public void setCreDate(Timestamp creDate) {
        this.creDate = creDate;
    }

    /**
     * get 更新人員
     */
    public String getUpdUserId() {
        return updUserId;
    }

    /**
     * set 更新人員
     */
    public void setUpdUserId(String updUserId) {
        this.updUserId = updUserId;
    }

    /**
     * get 更新人員名稱
     */
    public String getUpdUserName() {
        return updUserName;
    }

    /**
     * set 更新人員名稱
     */
    public void setUpdUserName(String updUserName) {
        this.updUserName = updUserName;
    }

    /**
     * get 更新日期
     */
    public Timestamp getUpdDate() {
        return updDate;
    }

    /**
     * set 更新日期
     */
    public void setUpdDate(Timestamp updDate) {
        this.updDate = updDate;
    }
    

    @Override
    public String getCode() {
        return this.empId;
    }

    @Override
    public String getName() {
        return this.empName;
    }

    @Override
    public String getDepCode() {
        return this.deptId;
    }

    @Override
    public String getDepName() {
        return this.deptName;
    }

    @Override
    public String getStatusDesc() {
        return this.enableFlg;
    }

    @Override
    public String getUpdater() {
        return this.updUserId;
    }

    @Override
    public Timestamp getUpdateTime() {
        return this.updDate;
    }

    @Override
    public List<? extends Role> getRoles() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return SimpleContextHolder.get(CapWebUtil.localeKey);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getStatus() {
        return this.enableFlg;
    }

    @Override
    public String getEmail() {
        return null;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getSupFlg() {
        return supFlg;
    }

    public void setSupFlg(String supFlg) {
        this.supFlg = supFlg;
    }

    public String getEnableFlg() {
        return enableFlg;
    }

    public void setEnableFlg(String enableFlg) {
        this.enableFlg = enableFlg;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getTelExt() {
        return telExt;
    }

    public void setTelExt(String telExt) {
        this.telExt = telExt;
    }

    public String getEmpPicName() {
        return empPicName;
    }

    public void setEmpPicName(String empPicName) {
        this.empPicName = empPicName;
    }

}
