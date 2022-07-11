/* 
 * CapNamedJdbcTemplate.java
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.jdbc;

import java.sql.BatchUpdateException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterBatchUpdateUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.iisigroup.cap.db.constants.CapJdbcConstants;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.exception.CapDBException;
import com.iisigroup.cap.db.model.Page;
import com.iisigroup.cap.db.utils.CapDbUtil;
import com.iisigroup.cap.jdbc.support.CapColumnMapRowMapper;
import com.iisigroup.cap.jdbc.support.CapRowMapperResultSetExtractor;
import com.iisigroup.cap.jdbc.support.CapSqlSearchQueryProvider;
import com.iisigroup.cap.jdbc.support.CapSqlStatement;
import com.iisigroup.cap.utils.SpelUtil;

/**
 * <pre>
 * CapNamedJdbcTemplate
 * </pre>
 * 
 * @since 2012/8/17
 * @author iristu
 * @version
 *          <ul>
 *          <li>2012/8/17,iristu,new
 *          <li>2016/6/16,sunkist,update for queryForInt() is deprecated
 *          </ul>
 */
public class CapNamedJdbcTemplate extends NamedParameterJdbcTemplate {

    // default
    private final Logger logger = LoggerFactory.getLogger(CapNamedJdbcTemplate.class);

    private final Logger piiLogger = LoggerFactory.getLogger(CapNamedJdbcTemplate.class.getName()+".piiLogger");

    private CapSqlStatement sqlp;
    private CapSqlStatement sqltemp;

    DataSource dataSource;

    public CapNamedJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public void setSqltemp(CapSqlStatement sqltemp) {
        this.sqltemp = sqltemp;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSqlProvider(CapSqlStatement privider) {
        this.sqlp = privider;
    }

    public void query(String sqlId, Map<String, ?> args, RowCallbackHandler rch) {
        StringBuffer sql = new StringBuffer((String) sqlp.getValue(sqlId, sqlId));
        if (!sql.toString().trim().toUpperCase(Locale.ENGLISH).startsWith("CALL")) {
            sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        }
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            super.query(sql.toString(), args, rch);
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    /**
     * 查詢
     * 
     * @param <T>
     *            T
     * @param sqlId
     *            sql id
     * @param appendDynamicSql
     *            append dynamic sql
     * @param args
     *            參數
     * @param startRow
     *            開始筆數
     * @param fetchSize
     *            截取筆數
     * @param rm
     *            RowMapper
     * @return List<T>
     */
    public <T> List<T> query(String sqlId, String appendDynamicSql, Map<String, Object> args, int startRow, int fetchSize, RowMapper<T> rm) {
        StringBuffer sql = new StringBuffer((String) sqlp.getValue(sqlId, sqlId));
        if (appendDynamicSql != null) {
            sql.append(' ').append(appendDynamicSql);
        }
        if (!sql.toString().trim().toUpperCase(Locale.ENGLISH).startsWith("CALL")) {
            sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        }
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            return super.query(sql.toString(), (Map<String, Object>) args, new CapRowMapperResultSetExtractor<T>(rm, startRow, fetchSize));
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    /**
     * 查詢，查詢結果為List<Map<key, value>>
     * 
     * @param <T>
     *            bean
     * @param sqlId
     *            sqlId
     * @param appendDynamicSql
     *            append sql
     * @param args
     *            傳入參數
     * @param rm
     *            RowMapper
     * 
     * @return List<T>
     */
    public <T> List<T> query(String sqlId, String appendDynamicSql, Map<String, ?> args, RowMapper<T> rm) {
        StringBuffer sql = new StringBuffer((String) sqlp.getValue(sqlId, sqlId));
        if (appendDynamicSql != null) {
            sql.append(' ').append(appendDynamicSql);
        }
        if (!sql.toString().trim().toUpperCase(Locale.ENGLISH).startsWith("CALL")) {
            sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        }
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            return super.query(sql.toString(), (Map<String, ?>) args, new RowMapperResultSetExtractor<T>(rm));
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    public List<Map<String, Object>> query(String sqlId, Map<String, Object> args) {
        return query(sqlId, null, args, new CapColumnMapRowMapper());
    }

    public List<Map<String, Object>> query(String sqlId, SearchSetting search) {
        return query(sqlId, search, new CapColumnMapRowMapper(), new HashMap<String, Object>());
    }

    /**
     * 查詢，查詢結果為JavaBean
     * 
     * @param <T>
     *            JavaBean
     * @param sqlId
     *            sqlId
     * @param appendDynamicSql
     *            append sql
     * @param rm
     *            RowMapper
     * @param args
     *            傳入參數
     * @return T
     * @throws GWException
     */
    public <T> T queryForObject(String sqlId, String appendDynamicSql, Map<String, ?> args, RowMapper<T> rm) {
        List<T> list = this.query(sqlId, appendDynamicSql, args, rm);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 查詢，查詢結果為Map<key,value>
     * 
     * @param sqlId
     *            sqlId
     * @param appendDynamicSql
     *            append sql
     * @param args
     *            傳入參數
     * @return Map<String, Object>
     * @throws GWException
     */
    public Map<String, Object> queryForMap(String sqlId, String appendDynamicSql, Map<String, ?> args) {
        return queryForObject(sqlId, appendDynamicSql, args, new CapColumnMapRowMapper());
    }

    /**
     * 查詢筆數專用
     * 
     * @param sqlId
     *            sqlId
     * @param args
     *            args
     * @return int
     * @throws GWException
     */
    public int queryForInt(String sqlId, Map<String, ?> args) {
        StringBuffer sql = new StringBuffer((String) sqlp.getValue(sqlId, sqlId));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            Integer result = super.queryForObject(sql.toString(), args, Integer.class);
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    /**
     * 查詢，查詢結果為Map<key,value>
     * 
     * @param sqlId
     *            sqlId
     * @param args
     *            傳入參數
     * @return Map<String, Object>
     * @throws GWException
     */
    public Map<String, Object> queryForMap(String sqlId, Map<String, ?> args) {
        return this.queryForMap(sqlId, null, args);
    }

    /**
     * 新增、修改、刪除
     * 
     * @param sqlId
     *            sqlId
     * @param args
     *            Map<String, ?>
     * @return int
     * @throws GWException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int update(String sqlId, Map<String, ?> args) {
        String sql = sqlp.getValue(sqlId, sqlId);
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n\t").append(CapDbUtil.convertToSQLCommand(sql, args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            return super.update(sql, (Map) args);
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    @SuppressWarnings("unchecked")
    public int batchUpdate(String sqlId, Map<String, Integer> sqlTypes, final List<Map<String, Object>> batchValues) {
        long cur = System.currentTimeMillis();
        try {
            int[] batchCount = null;
            String sql = sqlp.getValue(sqlId, sqlId);
            ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
            if (sqlTypes != null && !sqlTypes.isEmpty()) {
                MapSqlParameterSource[] batch = new MapSqlParameterSource[batchValues.size()];
                for (int i = 0; i < batchValues.size(); i++) {
                    Map<String, Object> valueMap = batchValues.get(i);
                    batch[i] = new MapSqlParameterSource();
                    for (Entry<String, Integer> entry : sqlTypes.entrySet()) {
                        if (!valueMap.containsKey(entry.getKey())) {
                            valueMap.put(entry.getKey(), null);
                        }
                        batch[i].addValue(entry.getKey(), valueMap.get(entry.getKey()), entry.getValue());
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n#").append((i + 1)).append("\t")
                                .append(CapDbUtil.convertToSQLCommand(sql, valueMap)).toString());
                    }
                }
                cur = System.currentTimeMillis();
                batchCount = NamedParameterBatchUpdateUtils.executeBatchUpdateWithNamedParameters(parsedSql, batch, super.getJdbcOperations());

            } else {
                SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(batchValues.toArray(new HashMap[batchValues.size()]));
                cur = System.currentTimeMillis();
                batchCount = NamedParameterBatchUpdateUtils.executeBatchUpdateWithNamedParameters(parsedSql, batch, super.getJdbcOperations());
            }
            int rows = 0;
            for (int i : batchCount) {
                rows += i;
            }
            return rows;
        } catch (Exception e) {
            Throwable cause = (Throwable) e;
            String msg = cause.getMessage();
            while ((cause = cause.getCause()) != null) {
                if (cause instanceof BatchUpdateException) {
                    cause = ((BatchUpdateException) cause).getNextException();
                }
                msg = cause.getMessage();
            }
            throw new CapDBException(sqlId + ": " + msg, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    /**
     * call SP
     * 
     * @param spName
     *            SP Name
     * @param params
     *            SqlParameter/SqlOutParameter/SqlInOutParameter obj array
     * @param inParams
     *            SqlParameter values
     * @return result map
     */
    public Map<String, Object> callSPForMap(String spName, SqlParameter[] params, Map<String, ?> inParams) {

        int idx = spName.indexOf(".");
        String schemaName = null;
        if (idx != -1) {
            schemaName = spName.substring(0, idx);
            spName = spName.substring(idx + 1);
        }

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName(spName).declareParameters(params);

        if (schemaName != null) {
            jdbcCall.setSchemaName(schemaName);
        }

        jdbcCall.setAccessCallParameterMetaData(false);

        Map<String, Object> out = jdbcCall.execute(inParams);
        return out;
    }

    /**
     * wrapper SqlRowSet queryForRowSet(String sql, Object[] args) from org.springframework.jdbc.core.JdbcTemplate
     * 
     * @param sqlId
     *            sqlId
     * @param args
     *            參數
     * @return SqlRowSet
     */
    public SqlRowSet queryForRowSet(String sqlId, Map<String, ?> args) {
        StringBuffer sql = new StringBuffer((String) sqlp.getValue(sqlId, sqlId));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("SqlId=").append(sqlp.containsKey(sqlId) ? sqlId : "").append("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            return super.queryForRowSet(sql.toString(), args);
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    public List<Map<String, Object>> queryPaging(String sqlId, Map<String, Object> args, int startRow, int fetchSize) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, getSourceSql(sqlId, args, startRow, fetchSize));
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_ORDER, sqltemp.getValue(CapJdbcConstants.SQL_PAGING_DUMMY_ORDER_BY, ""));
        StringBuffer sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_QUERY), params, sqltemp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (args == null) {
            args = new HashMap<String, Object>();
        }
        args.put("startRow", startRow);
        args.put("endRow", startRow + fetchSize);
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            return super.queryForList(sql.toString(), args);
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    public Page<Map<String, Object>> queryForPage(String sqlId, Map<String, Object> args, int startRow, int fetchSize) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, getSourceSql(sqlId, args, startRow, fetchSize));
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_ORDER, sqltemp.getValue(CapJdbcConstants.SQL_PAGING_DUMMY_ORDER_BY, ""));
        StringBuffer sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_TOTAL_PAGE), params, sqlp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), args)).toString());
        }
        // find list
        List<Map<String, Object>> list = this.queryPaging(sqlId, args, startRow, fetchSize);
        long cur = System.currentTimeMillis();
        try {
            return new Page<Map<String, Object>>(list, super.queryForObject(sql.toString(), args, Integer.class), fetchSize, startRow);
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    private String getSourceSql(String sqlId, Map<String, Object> args, int startRow, int fetchSize) {
        // Rewrite sql map. (ex: SQL Server => top n)
        String countAllSqlId = sqlId + ".countAll()";
        if (sqltemp.containsKey(sqlId)) {
            Map<String, Object> preParams = new HashMap<String, Object>();
            if (sqltemp.containsKey(countAllSqlId)) {
                preParams.put("countAll", sqltemp.getValue(countAllSqlId, countAllSqlId));
            }
            return SpelUtil.spelParser(sqltemp.getValue(sqlId, sqlId), preParams, sqltemp.getParserContext());
        } else {
            return sqlp.getValue(sqlId, sqlId);
        }
    }

    public Page<Map<String, Object>> queryForPage(String sqlId, SearchSetting search) {
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        String _sql = sqlp.getValue(sqlId, sqlId);
        // 加入 SpEL 處理 where clause
        String whereClause = provider.generateWhereClause();
        if (logger.isTraceEnabled()) {
            logger.trace("whereClause: " + whereClause);
        }
        StringBuffer sourceSql = new StringBuffer(processWhereClause(whereClause, _sql));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, sourceSql.toString());
        // 準備查詢筆數sql
        StringBuffer sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_TOTAL_PAGE), params, sqlp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), provider.getParams())).toString());
        }
        String sqlRow = sql.toString();
        // 準備查詢list sql
        // sourceSql.append(provider.generateOrderCause());
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, sourceSql.toString());
        String orderBy = search.hasOrderBy() ? provider.generateOrderClause() : sqltemp.getValue(CapJdbcConstants.SQL_PAGING_DUMMY_ORDER_BY, "");
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_ORDER, orderBy);
        sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_QUERY), params, sqlp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        // 此處的 order by 是組完分頁 sql 後，再做一次 order by，因為子查詢中的 order by 不會反映在最後的查詢結果
        sql.append(provider.generateOrderClause());
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), provider.getParams())).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            int totalRows = super.queryForObject(sqlRow, provider.getParams(), Integer.class);
            List<Map<String, Object>> list = super.queryForList(sql.toString(), provider.getParams());
            return new Page<Map<String, Object>>(list, totalRows, search.getMaxResults(), search.getFirstResult());
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    public <T> Page<T> queryForPage(String sqlId, SearchSetting search, RowMapper<T> rm) {
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        String _sql = sqlp.getValue(sqlId, sqlId);
        // 加入 SpEL 處理 where clause
        String whereClause = provider.generateWhereClause();
        StringBuffer sourceSql = new StringBuffer(processWhereClause(whereClause, _sql));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, sourceSql.toString());
        // 準備查詢筆數sql
        StringBuffer sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_TOTAL_PAGE), params, sqlp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), provider.getParams())).toString());
        }
        String sqlRow = sql.toString();
        // 準備查詢list sql
        // sourceSql.append(provider.generateOrderCause());
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, sourceSql.toString());
        String orderBy = search.hasOrderBy() ? provider.generateOrderClause() : sqltemp.getValue(CapJdbcConstants.SQL_PAGING_DUMMY_ORDER_BY, "");
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_ORDER, orderBy);
        sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_QUERY), params, sqlp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        // 此處的 order by 是組完分頁 sql 後，再做一次 order by，因為子查詢中的 order by 不會反映在最後的查詢結果
        sql.append(provider.generateOrderClause());
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), provider.getParams())).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            int totalRows = super.queryForObject(sqlRow, provider.getParams(), Integer.class);
            List<T> list = super.query(sql.toString(), provider.getParams(), rm);
            return new Page<T>(list, totalRows, search.getMaxResults(), search.getFirstResult());
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    public <T> Page<T> queryForPage(String sqlId, SearchSetting search, RowMapper<T> rm, Map<String, Object> inSqlParam) {
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        String _sql = sqlp.getValue(sqlId, sqlId);
        // 加入 SpEL 處理 where clause
        String whereClause = provider.generateWhereClause();
        StringBuffer sourceSql = new StringBuffer(processWhereClause(whereClause, _sql));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, sourceSql.toString());
        // 準備查詢筆數sql
        StringBuffer sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_TOTAL_PAGE), params, sqlp.getParserContext()));
        Map<String, Object> param = provider.getParams();
        param.putAll(inSqlParam);
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), param)).toString());
        }
        String sqlRow = sql.toString();
        // 準備查詢list sql
        // sourceSql.append(provider.generateOrderCause());
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_SQL, sourceSql.toString());
        String orderBy = search.hasOrderBy() ? provider.generateOrderClause() : sqltemp.getValue(CapJdbcConstants.SQL_PAGING_DUMMY_ORDER_BY, "");
        params.put(CapJdbcConstants.SQL_PAGING_SOURCE_ORDER, orderBy);
        sql = new StringBuffer().append(SpelUtil.spelParser((String) sqltemp.getValue(CapJdbcConstants.SQL_PAGING_QUERY), params, sqlp.getParserContext()));
        sql.append(' ').append(sqltemp.getValue(CapJdbcConstants.SQL_QUERY_SUFFIX, ""));
        // 此處的 order by 是組完分頁 sql 後，再做一次 order by，因為子查詢中的 order by 不會反映在最後的查詢結果
        sql.append(provider.generateOrderClause());
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sql.toString(), param)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            int totalRows = super.queryForObject(sqlRow, param, Integer.class);
            List<T> list = super.query(sql.toString(), param, rm);
            return new Page<T>(list, totalRows, search.getMaxResults(), search.getFirstResult());
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }
    
    public <T> List<T> query(String sqlId, SearchSetting search, RowMapper<T> rm, Map<String, Object> inSqlParam) {
        CapSqlSearchQueryProvider provider = new CapSqlSearchQueryProvider(search);
        String _sql = sqlp.getValue(sqlId, sqlId);
        // 加入 SpEL 處理 where clause
        String whereClause = provider.generateWhereClause();
        StringBuffer sourceSql = new StringBuffer(processWhereClause(whereClause, _sql));
        sourceSql.append(provider.generateOrderClause());
        Map<String, Object> param = provider.getParams();
        param.putAll(inSqlParam);
        if (logger.isTraceEnabled()) {
            logger.trace(new StringBuffer("\n\t").append(CapDbUtil.convertToSQLCommand(sourceSql.toString(), param)).toString());
        }
        long cur = System.currentTimeMillis();
        try {
            return super.query(sourceSql.toString(), param, rm);
        } catch (Exception e) {
            throw new CapDBException(sqlId, e, getClass());
        } finally {
            logger.info("CapNamedJdbcTemplate spend {} ms", (System.currentTimeMillis() - cur));
        }
    }

    private String processWhereClause(String whereClause, String _sql) {
        String result = null;
        if (_sql.contains(CapJdbcConstants.SQL_SEARCH_SETTING_WHERE_CLAUSE)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CapJdbcConstants.SQL_SEARCH_SETTING_WHERE_CLAUSE, whereClause);
            result = SpelUtil.spelParser(_sql, params, sqlp.getParserContext());
        } else {
            StringBuffer sourceSql = new StringBuffer(_sql).append(_sql.toUpperCase(Locale.ENGLISH).lastIndexOf("WHERE") > 0 ? " AND " : " WHERE ").append(whereClause);
            result = sourceSql.toString();
        }
        return result;
    }
}// ~
