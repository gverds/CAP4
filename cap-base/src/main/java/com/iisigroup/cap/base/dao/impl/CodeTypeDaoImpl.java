/*
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * 11F, No.133, Sec.4, Minsheng E. Rd., Taipei, 10574, Taiwan, R.O.C.
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */

package com.iisigroup.cap.base.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iisigroup.cap.base.dao.CodeTypeDao;
import com.iisigroup.cap.base.model.CodeType;
import com.iisigroup.cap.db.constants.SearchMode;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.dao.impl.GenericDaoImpl;

/**
 * <pre>
 * 代碥表DAO
 * </pre>
 * 
 * @since 2010/12/9
 * @author iristu
 * @version
 *          <ul>
 *          <li>2010/12/9,iristu,new
 *          <li>2011/11/20,RodesChen,from cap
 *          </ul>
 */
@Repository
public class CodeTypeDaoImpl extends GenericDaoImpl<CodeType> implements CodeTypeDao {

    /*
     * (non-Javadoc)
     * @see com.iisigroup.cap.base.dao.CodeTypeDao#findByCodeType(java.lang.String, java.lang.String)
     */
    @Override
    public List<CodeType> findByCodeType(String codetype, String locale) {
        SearchSetting search = createSearchTemplete();
        search.addSearchModeParameters(SearchMode.EQUALS, "locale", locale);
        search.addSearchModeParameters(SearchMode.EQUALS, "codeType", codetype);
        search.setFirstResult(0).setMaxResults(Integer.MAX_VALUE);
        search.addOrderBy("codeOrder");
        List<CodeType> list = find(search);
        return list;
    }

    /*
     * (non-Javadoc)
     * @see com.iisigroup.cap.base.dao.CodeTypeDao#findByCodeTypeAndCodeValue(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public CodeType findByCodeTypeAndCodeValue(String cType, String cValue, String locale) {
        SearchSetting search = createSearchTemplete();
        search.addSearchModeParameters(SearchMode.EQUALS, "locale", locale);
        search.addSearchModeParameters(SearchMode.EQUALS, "codeType", cType);
        search.addSearchModeParameters(SearchMode.EQUALS, "codeValue", cValue);
        search.setFirstResult(0).setMaxResults(Integer.MAX_VALUE);
        return findUniqueOrNone(search);
    }

    /*
     * (non-Javadoc)
     * @see com.iisigroup.cap.base.dao.CodeTypeDao#findByCodeType(java.lang.String[], java.lang.String)
     */
    @Override
    public List<CodeType> findByCodeType(String[] codetypes, String locale) {
        SearchSetting search = createSearchTemplete();
        search.addSearchModeParameters(SearchMode.EQUALS, "locale", locale);
        search.addSearchModeParameters(SearchMode.IN, "codeType", codetypes);
        search.setFirstResult(0).setMaxResults(Integer.MAX_VALUE);
        search.addOrderBy("codeOrder");
        return find(search);
    }

    /*
     * (non-Javadoc)
     * @see com.iisigroup.cap.base.dao.CodeTypeDao#findByCodeTypeAndCodeDesc(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<CodeType> findByCodeTypeAndCodeDesc(String cType, String codeDesc, String locale) {
        SearchSetting search = createSearchTemplete();
        search.addSearchModeParameters(SearchMode.EQUALS, "locale", locale);
        search.addSearchModeParameters(SearchMode.EQUALS, "codeType", cType);
        search.addSearchModeParameters(SearchMode.EQUALS, "codeDesc", codeDesc);
        return find(search);
    }

    /*
     * (non-Javadoc)
     * @see com.iisigroup.cap.base.dao.CodeTypeDao#findByOid(java.lang.String)
     */
    @Override
    public CodeType findByOid(String oid) {
        return find(oid);
    }

}
