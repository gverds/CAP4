/* 
 * GenericDaoImpl.java
 * 
 * Copyright (c) 2011 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.util.Assert;

import com.iisigroup.cap.db.annotation.Table;
import com.iisigroup.cap.db.constants.CapJdbcConstants;
import com.iisigroup.cap.db.dao.GenericDao;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.exception.CapDBException;
import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.db.model.Page;
import com.iisigroup.cap.db.utils.CapDbUtil;
import com.iisigroup.cap.db.utils.CapEntityUtil;
import com.iisigroup.cap.jdbc.CapNamedJdbcTemplate;
import com.iisigroup.cap.jdbc.support.CapSqlSearchQueryProvider;
import com.iisigroup.cap.jdbc.support.CapSqlStatement;
import com.iisigroup.cap.model.GenericBean;
import com.iisigroup.cap.utils.CapBeanUtil;
import com.iisigroup.cap.utils.CapCommonUtil;

/**
 * <pre>
 * Dao
 * </pre>
 * 
 * @since 2011/11/1
 * @author rodeschen
 * @version
 *          <ul>
 *          <li>2011/11/1,rodeschen,new
 *          <li>2011/11/20,gabriella,modify
 *          <li>2012/2/14,RodesChen,add resource name
 *          <li>2012/6/29,RodesChen,add findUniqueOrNone setMaxResults 1
 *          <li>2013/5/21,增加SearchMode or & and 的查詢設定
 *          <li>2016/07/12,yunglin,#521 apply distinct from searchsetting
 *          </ul>
 * @param <T>
 */
public class GenericDaoImpl<T> implements GenericDao<T> {

    protected Class<T> type;
    protected Logger logger;
    @Resource(name = "capDml")
    private CapSqlStatement sqltemp;
    @Resource(name = "capSqlStatement")
    private CapSqlStatement paging;

    @Resource(name = "capJdbcTemplate")
    private CapNamedJdbcTemplate namedJdbcTemplate;

    @SuppressWarnings("unchecked")
    public GenericDaoImpl() {
        try {
            type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            Class<T> clazz = (Class<T>) getClass().getGenericSuperclass();
            type = (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
        }
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Insert.
     * 
     * @param entity
     *            the entry
     */
    public void save(Object entity) {
        Assert.notNull(entity, "The entity to save cannot be null element");
        if (find(entity) == null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, entity.getClass().getAnnotation(Table.class).name());
            params.put(CapJdbcConstants.SQL_DML_COLUMNS, getCombineColumnString(entity));
            params.put(CapJdbcConstants.SQL_DML_VALUES, getCombineParameterString(entity));
            StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_INSERT), params, sqltemp.getParserContext()));
            ((DataObject) entity).setOid(UUID.randomUUID().toString().replace("-", ""));
            Map<String, Object> args = CapBeanUtil.bean2Map((GenericBean) entity, CapEntityUtil.getColumnName(entity));
            getNamedJdbcTemplate().execute(sql.toString(), args, new PreparedStatementCallback<Boolean>() {
                @Override
                public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    return ps.execute();
                }
            });
        } else {
            merge(entity);
        }
    }

    public void save(List<?> entries) {
        for (Object entity : entries) {
            Assert.notNull(entity, "The List must not contain null element");
            save(entity);
        }
    }

    public void merge(Object entity) {
        Assert.notNull(entity, "The entity to save cannot be null element");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, entity.getClass().getAnnotation(Table.class).name());
        String[] detail = getCombineUpdateInfoString(entity, entity.getClass().getAnnotation(Table.class).pkColumn());
        params.put(CapJdbcConstants.SQL_DML_UPDATE_INFO, detail[0]);
        params.put(CapJdbcConstants.SQL_DML_WHERE_CLAUSE, detail[1]);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_UPDATE), params, sqltemp.getParserContext()));
        Map<String, Object> args = CapBeanUtil.bean2Map((GenericBean) entity, CapEntityUtil.getColumnName(entity));
        getNamedJdbcTemplate().execute(sql.toString(), args, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                return ps.execute();
            }
        });
    }

    /**
     * Delete.
     * 
     * @param entity
     *            the entry
     */
    public void delete(Object entity) {
        Map<String, Object> params = new HashMap<String, Object>();
        String pkColumn = getType().getAnnotation(Table.class).pkColumn();
        String pkField = getPkField(entity);
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, getType().getAnnotation(Table.class).name());
        params.put(CapJdbcConstants.SQL_DML_WHERE_CLAUSE, pkColumn + "=:" + pkField);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_DELETE), params, sqltemp.getParserContext()));
        Map<String, Object> args = new HashMap<String, Object>();
        args.put(pkField, getPrimaryKey(entity));
        getNamedJdbcTemplate().execute(sql.toString(), args, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                return ps.execute();
            }
        });
    }

    public void delete(List<?> entries) {
        for (Object entity : entries) {
            delete(entity);
        }
    }

    /**
     * Find.
     * 
     * @param pk
     *            the oid
     * 
     * @return the t
     */
    public T find(Serializable pk) {
        Object entity = null;
        try {
            entity = type.newInstance();
        } catch (Exception e) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        String pkColumn = type.getAnnotation(Table.class).pkColumn();
        String pkField = getPkField(entity);
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, type.getAnnotation(Table.class).name());
        params.put(CapJdbcConstants.SQL_DML_COLUMNS, getCombineColumnString(entity));
        params.put(CapJdbcConstants.SQL_DML_WHERE_CLAUSE, pkColumn + "=:" + pkField);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_SELECT), params, sqltemp.getParserContext()));
        Map<String, Object> args = new HashMap<String, Object>();
        args.put(pkField, pk);
        List<T> rs = getNamedJdbcTemplate().query(sql.toString(), args, new BeanPropertyRowMapper<T>(type));
        return rs.isEmpty() ? null : rs.get(0);
    }

    public Serializable getPrimaryKey(Object model) {
        if (model instanceof DataObject) {
            return (Serializable) ((DataObject) model).getOid();
        } else {
            return null;
        }
    }

    public T find(Object entity) {
        Serializable pk = getPrimaryKey(entity);
        if (pk == null) {
            return null;
        }
        return find(pk);
    }

    public T findUniqueOrNone(SearchSetting search) {
        search.setFirstResult(0).setMaxResults(1);
        List<T> models = find(getType(), search);
        if (models != null && !models.isEmpty()) {
            return models.iterator().next();
        }
        return null;
    }

    @Override
    public int count(SearchSetting search) {
        return count(getType(), search);
    }

    public List<T> find(final SearchSetting search) {
        Object entity = null;
        try {
            entity = type.newInstance();
        } catch (Exception e) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, type.getAnnotation(Table.class).name());
        params.put(CapJdbcConstants.SQL_DML_COLUMNS, getCombineColumnString(entity));
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_SELECT), params, sqltemp.getParserContext()))
                .append(provider.generateWhereClause()).append(provider.generateOrderClause());
        return getNamedJdbcTemplate().query(sql.toString(), provider.getParams(), new BeanPropertyRowMapper<T>(type));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iisigroup.cap.db.dao.GenericDao#findPage(com.iisigroup.cap.db.dao.SearchSetting)
     */
    public Page<T> findPage(SearchSetting search) {
        return findPage(getType(), search);
    }

    public Class<T> getType() {
        return type;
    }

    @SuppressWarnings("rawtypes")
    public GenericDaoImpl setType(Class<T> type) {
        this.type = type;
        return this;
    }

    public SearchSetting createSearchTemplete() {
        return new SearchSettingImpl();
    }

    protected CapNamedJdbcTemplate getNamedJdbcTemplate() {
        return namedJdbcTemplate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iisigroup.cap.db.dao.GenericDao#findById(java.lang.Class, java.io.Serializable)
     */
    @Override
    public <S> S findById(Class<S> clazz, Serializable pk) {
        Object entity = null;
        try {
            entity = clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        String pkColumn = clazz.getAnnotation(Table.class).pkColumn();
        String pkField = getPkField(entity);
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, clazz.getAnnotation(Table.class).name());
        params.put(CapJdbcConstants.SQL_DML_COLUMNS, getCombineColumnString(entity));
        params.put(CapJdbcConstants.SQL_DML_WHERE_CLAUSE, pkColumn + "=:" + pkField);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_SELECT), params, sqltemp.getParserContext()));
        Map<String, Object> args = new HashMap<String, Object>();
        args.put(pkField, pk);
        List<S> rs = getNamedJdbcTemplate().query(sql.toString(), args, new BeanPropertyRowMapper<S>(clazz));
        return rs.isEmpty() ? null : rs.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iisigroup.cap.db.dao.GenericDao#find(java.lang.Class, com.iisigroup.cap.db.dao.SearchSetting)
     */
    @Override
    public <S> List<S> find(Class<S> clazz, final SearchSetting search) {
        Object entity = null;
        try {
            entity = clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, clazz.getAnnotation(Table.class).name());
        params.put(CapJdbcConstants.SQL_DML_COLUMNS, getCombineColumnString(entity));
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_SELECT), params, sqltemp.getParserContext()))
                .append(provider.generateWhereClause()).append(provider.generateOrderClause());
        return getNamedJdbcTemplate().query(sql.toString(), provider.getParams(), new BeanPropertyRowMapper<>(clazz));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iisigroup.cap.db.dao.GenericDao#findPage(java.lang.Class, com.iisigroup.cap.db.dao.SearchSetting)
     */
    @Override
    public <S> Page<S> findPage(Class<S> clazz, SearchSetting search) {
        Object entity = null;
        try {
            entity = clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
        Map<String, Object> orignalSqlParam = new HashMap<String, Object>();
        orignalSqlParam.put(CapJdbcConstants.SQL_DML_TABLE_NAME, clazz.getAnnotation(Table.class).name());
        orignalSqlParam.put(CapJdbcConstants.SQL_DML_COLUMNS, getCombineColumnString(entity));
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        StringBuffer orignalSql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_SELECT), orignalSqlParam, sqltemp.getParserContext()))
                .append(provider.generateWhereClause());
        String _sql = orignalSql.toString();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, _sql);
        // 準備查詢筆數sql
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) paging.getValue(CapJdbcConstants.SQL_PAGING_TOTAL_PAGE), params, sqltemp.getParserContext()));
        sql.append(' ').append(paging.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), provider.getParams())).toString());
        }
        String sqlRow = sql.toString();
        // 準備查詢list sql
        String orderBy = search.hasOrderBy() ? provider.generateOrderClause() : "";
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_ORDER, orderBy);
        sql = new StringBuffer().append(CapCommonUtil.spelParser((String) paging.getValue(CapJdbcConstants.SQL_PAGING_QUERY), params, sqltemp.getParserContext()));
        sql.append(' ').append(paging.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        // 此處的 order by 是組完分頁 sql 後，再做一次 order by，因為子查詢中的 order by 不會反映在最後的查詢結果
        sql.append(provider.generateOrderClause());
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), provider.getParams())).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            int totalRows = getNamedJdbcTemplate().queryForObject(sqlRow, provider.getParams(), Integer.class);
            List<S> list = getNamedJdbcTemplate().query(sql.toString(), provider.getParams(), new BeanPropertyRowMapper<S>(clazz));
            return new Page<S>(list, totalRows, search.getMaxResults(), search.getFirstResult());
        } catch (Exception e) {
            throw new CapDBException(e, clazz);
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    /**
     * 取得筆數
     * 
     * @param clazz
     * @param search
     * @return
     */
    private <S> int count(Class<S> clazz, SearchSetting search) {
        Map<String, Object> params = new HashMap<String, Object>();
        String pkColumn = clazz.getAnnotation(Table.class).pkColumn();
        params.put(CapJdbcConstants.SQL_DML_TABLE_NAME, clazz.getAnnotation(Table.class).name());
        params.put(CapJdbcConstants.SQL_DML_COLUMNS, "COUNT(" + pkColumn + ")");
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        StringBuffer sql = new StringBuffer().append(CapCommonUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_DML_SELECT), params, sqltemp.getParserContext()))
                .append(provider.generateWhereClause());
        return getNamedJdbcTemplate().queryForObject(sql.toString(), provider.getParams(), Integer.class);
    }

    private String getCombineColumnString(Object entity) {
        String[] columnNames = CapEntityUtil.getColumnName(entity, true);
        StringBuilder sb = new StringBuilder();
        for (String column : columnNames) {
            sb.append(column).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private String getCombineParameterString(Object entity) {
        String[] columnNames = CapEntityUtil.getColumnName(entity, false);
        StringBuilder sb = new StringBuilder();
        for (String column : columnNames) {
            sb.append(":").append(column).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private String[] getCombineUpdateInfoString(Object entity, String pkColumn) {
        String[] result = new String[2];
        String[] columns = CapEntityUtil.getColumnName(entity, true);
        String[] params = CapEntityUtil.getColumnName(entity, false);
        StringBuilder updateInfo = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String param = params[i];
            if (!column.equalsIgnoreCase(pkColumn)) {
                updateInfo.append(column).append("=:").append(param).append(", ");
            } else {
                whereClause.append(column).append("=:").append(param);
            }
        }
        updateInfo.delete(updateInfo.length() - 2, updateInfo.length());
        result[0] = updateInfo.toString();
        result[1] = whereClause.toString();
        return result;
    }

    private String getPkField(Object entity) {
        String[] columns = CapEntityUtil.getColumnName(entity, true);
        String[] params = CapEntityUtil.getColumnName(entity, false);
        String pkColumn = entity.getClass().getAnnotation(Table.class).pkColumn();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if (column.equalsIgnoreCase(pkColumn)) {
                return params[i];
            }
        }
        return null;
    }

}
