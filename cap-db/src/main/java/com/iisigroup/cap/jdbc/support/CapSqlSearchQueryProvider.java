/* 
 * CapSqlSearchProd.java
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.jdbc.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.CollectionUtils;

import com.iisigroup.cap.db.constants.SearchMode;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.model.SearchModeParameter;

/**
 * <pre>
 * JdbcTemplate SqlSearchQueryProvider
 * </pre>
 * 
 * @since 2012/11/7
 * @author iristu
 * @version
 *          <ul>
 *          <li>2012/11/7,iristu,new
 *          <li>2015/09/18,sunkist,update
 *          <li>2015/09/25,sunkist,避免參數重覆問題
 *          </ul>
 */
public class CapSqlSearchQueryProvider {

    Map<String, Object> params;
    SearchSetting search;

    public CapSqlSearchQueryProvider(SearchSetting search) {
        this.search = search;
        this.params = new HashMap<String, Object>();
        params.put("startRow", search.getFirstResult());
        params.put("endRow", search.getFirstResult() + search.getMaxResults());
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String generateWhereClause() {
        StringBuffer sb = new StringBuffer();
        if (!CollectionUtils.isEmpty(search.getSearchModeParameters())) {
            for (SearchModeParameter s : search.getSearchModeParameters()) {
                Object _key = s.getKey();
                Object _value = s.getValue();
                if (_key instanceof SearchModeParameter && _value instanceof SearchModeParameter) {
                    SearchModeParameter k = (SearchModeParameter) _key;
                    SearchModeParameter v = (SearchModeParameter) _value;
                    List<SearchModeParameter> mySearchPool = new ArrayList<SearchModeParameter>();
                    List<SearchMode> modes = new ArrayList<SearchMode>();

                    // add all of key
                    getRecursiveSearchModeParameterKey(mySearchPool, k, modes);
                    getRecursiveSearchModeParameterValue(mySearchPool, v, modes);
                    modes.add(s.getMode());

                    // append where string
                    if (!mySearchPool.isEmpty()) {
                        sb.append('(');
                    }
                    for (int i = 0; i < mySearchPool.size(); i++) {
                        if (i != 0) {
                            String mode = "";
                            if (SearchMode.OR == modes.get(i - 1)) {
                                mode = " or ";
                            } else if (SearchMode.AND == modes.get(i - 1)) {
                                mode = " and ";
                            }
                            sb.append(mode);
                        }
                        sb.append(generateItemQuery(mySearchPool.get(i)));
                    }
                    if (!mySearchPool.isEmpty()) {
                        sb.append(')');
                    }
                } else {
                    sb.append(generateItemQuery(s));
                }
                sb.append(" and ");
            }
            int len = sb.length();
            sb.delete(len - 5, len); // 拿掉最後一個" and "
        } else {
            sb.append("1 = 1");
        }

        return sb.toString();
    }

    private List<SearchMode> getRecursiveSearchModeParameterKey(List<SearchModeParameter> mySearchPool, SearchModeParameter k, List<SearchMode> modes) {
        if (k.getKey() instanceof SearchModeParameter) {
            getRecursiveSearchModeParameterKey(mySearchPool, (SearchModeParameter) k.getKey(), modes);
            modes.add(k.getMode());
            getRecursiveSearchModeParameterValue(mySearchPool, (SearchModeParameter) k.getValue(), modes);
        } else {
            mySearchPool.add(k);
        }
        return modes;
    }

    private List<SearchMode> getRecursiveSearchModeParameterValue(List<SearchModeParameter> mySearchPool, SearchModeParameter v, List<SearchMode> modes) {
        if (v.getValue() instanceof SearchModeParameter) {
            getRecursiveSearchModeParameterValue(mySearchPool, (SearchModeParameter) v.getValue(), modes);
            modes.add(v.getMode());
            getRecursiveSearchModeParameterKey(mySearchPool, (SearchModeParameter) v.getKey(), modes);
        } else {
            mySearchPool.add(v);
        }
        return modes;
    }

    public String generateOrderClause() {
        StringBuffer sb = new StringBuffer();
        if (search.hasOrderBy()) {
            sb.append(" order by ");
            Map<String, Boolean> orderMap = search.getOrderBy();
            for (Entry<String, Boolean> entry : orderMap.entrySet()) {
                sb.append(entry.getKey());
                if (entry.getValue()) {
                    sb.append(" desc ");
                }
                sb.append(',');
            }
            int len = sb.length();
            sb.delete(len - 1, len); // 拿掉最後一個","
        }
        return sb.toString();
    }

    private String generateItemQuery(SearchModeParameter search) {
        String key = search.getKey();
        // 當 search.hashCode() 為負數時，會發生
        // Caused by: org.springframework.dao.InvalidDataAccessApiUsageException: No value supplied for the SQL parameter 'XXXX': No value registered for key 'XXXX' 的錯誤
        String paramKey = key + Math.abs(search.hashCode());
        Object value = search.getValue();
        StringBuffer sb = new StringBuffer();
        switch (search.getMode()) {
        case BETWEEN:
            Object[] values = asArray(value);
            if (values != null) {
                sb.append(key).append(" between :").append(paramKey).append("1 and :").append(paramKey).append('2');
                params.put(paramKey + "1", values[0]);
                params.put(paramKey + "2", values[1]);
            }
            break;
        case GREATER_THAN:
            sb.append(key).append(" > :").append(paramKey);
            params.put(paramKey, value);
            break;
        case GREATER_EQUALS:
            sb.append(key).append(" >= :").append(paramKey);
            params.put(paramKey, value);
            break;
        case LESS_THAN:
            sb.append(key).append(" < :").append(paramKey);
            params.put(paramKey, value);
            break;
        case LESS_EQUALS:
            sb.append(key).append(" <= :").append(paramKey);
            params.put(paramKey, value);
            break;
        case IS_NULL:
            sb.append(key).append(" is null ");
            break;
        case IS_NOT_NULL:
            sb.append(key).append(" is not null ");
            break;
        case IN:
            sb.append(key).append(" in :").append(paramKey);
            params.put(paramKey, asCollection(value));
            break;
        case NOT_IN:
            sb.append(key).append(" not in :").append(paramKey);
            params.put(paramKey, asCollection(value));
            break;
        case LIKE:
            sb.append(key).append(" like :").append(paramKey);
            params.put(paramKey, value);
            break;
        case NOT_LIKE:
            sb.append(key).append(" not like :").append(paramKey);
            params.put(paramKey, value);
            break;
        case EQUALS:
            sb.append(key).append(" = :").append(paramKey);
            params.put(paramKey, value);
            break;
        case NOT_EQUALS:
            sb.append(key).append(" != :").append(paramKey);
            params.put(paramKey, value);
            break;
        default:
            break;
        }
        return sb.toString();
    }

    private Collection<?> asCollection(Object value) {
        if (value instanceof Collection) {
            return (Collection<?>) value;
        } else if (value.getClass().isArray()) {
            return Arrays.asList(value);
        }
        return Arrays.asList(value);
    }

    @SuppressWarnings("rawtypes")
    private Object[] asArray(Object value) {
        if (value.getClass().isArray()) {
            Object[] result = new Object[Array.getLength(value)];
            for (int i = 0; i < result.length; ++i) {
                result[i] = Array.get(value, i);
            }
            return result;
        } else if (value instanceof Collection) {
            return ((Collection) value).toArray();
        }
        return null;
    }

}// ~
