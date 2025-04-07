package com.iisigroup.cap.batch.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.usertype.DynamicParameterizedType.ParameterType;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.iisigroup.cap.batch.dao.BatchExecutionDao;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.dao.impl.GenericDaoImpl;
import com.iisigroup.cap.db.model.Page;

@Repository
public class BatchExecutionDaoImpl extends GenericDaoImpl<Object> implements BatchExecutionDao {

    @Override
    public Page<Map<String, Object>> findExecutionsForPage(SearchSetting search) {
        return getNamedJdbcTemplate().queryForPage("JobExecution.findPage", search);
    }

    @Override
    public List<Map<String, Object>> findStepsById(String executionId) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("jobExecutionId", executionId);
        return getNamedJdbcTemplate().query("stepExecution.findByExId", args);
    }

    @Override
    public Map<String, Object> findExecutionDetailById(String executionId) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("jobExecutionId", executionId);
        return getNamedJdbcTemplate().queryForMap("jobExecution.findById", args);
    }

    @Override
    public JobParameters findJobParamsById(String executionId) {
        final Map<String, JobParameter<?>> map = new HashMap<String, JobParameter<?>>();
        RowCallbackHandler handler = new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
            	String type = rs.getString("TYPE_CD");
                JobParameter value = null;
                if (type.equalsIgnoreCase("STRING")) {
                    value = new JobParameter(rs.getString("STRING_VAL"), String.class);
                } else if (type.equalsIgnoreCase("LONG")) {
                    value = new JobParameter(rs.getLong("LONG_VAL"), Long.class);
                } else if (type.equalsIgnoreCase("DOUBLE")) {
                    value = new JobParameter(rs.getDouble("DOUBLE_VAL"), Double.class);
                } else if (type.equalsIgnoreCase("DATE")) {
                    value = new JobParameter(rs.getTimestamp("DATE_VAL"), Timestamp.class);
                }
                map.put(rs.getString("KEY_NAME"), value);
            }
        };
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("jobExecutionId", executionId);
        getNamedJdbcTemplate().query("jobParams.findById", args, handler);
        return new JobParameters(map);
    }

    @Override
    public void updateExecution(Long executionId, String executor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobExecutionId", executionId);
        map.put("executor", executor);
        getNamedJdbcTemplate().update("jobExecution.updateById", map);
    }

}
