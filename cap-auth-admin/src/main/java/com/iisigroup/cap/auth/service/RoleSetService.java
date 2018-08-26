/*
 * RoleSetService.java
 *
 * Copyright (c) 2011 International Integrated System, Inc.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.auth.service;

import java.util.List;
import java.util.Map;

import com.iisigroup.cap.auth.model.DefaultRole;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.model.Page;

/**
 * <pre>
 * 系統功能維護
 * </pre>
 * 
 * @since 2014/1/16
 * @author tammy
 * @version
 *          <ul>
 *          <li>2014/1/16,tammy,new
 *          </ul>
 */
public interface RoleSetService {

    Page<Map<String, Object>> findPageUser(SearchSetting search, String roleCode);

    Page<Map<String, Object>> findPageEditUsr(SearchSetting search, String roleCode, String depCode);

    Page<Map<String, Object>> findPageFunc(SearchSetting search, String roleCode);

    Page<Map<String, Object>> findPageEditFunc(SearchSetting search, String roleCode, String sysType, String parent);

    Map<String, String> findAllDepartment();

    Map<String, String> findAllFunc(String sysType);

    int deleteUrList(String roleCode, List<String> delUsr);

    int deleteRfList(String roleCode, List<String> delFunc);

    List<Map<String, Object>> findAllRoleWithSelectedByUserCode(String userCode);

    DefaultRole findRoleByCode(String code);
}
