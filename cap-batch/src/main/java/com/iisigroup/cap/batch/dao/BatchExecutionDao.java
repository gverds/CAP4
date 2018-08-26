/*
 * BatchExecutionDao.java
 *
 * Copyright (c) 2011 International Integrated System, Inc.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.batch.dao;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.JobParameters;

import com.iisigroup.cap.db.dao.GenericDao;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.model.Page;

public interface BatchExecutionDao extends GenericDao<Object> {
    Page<Map<String, Object>> findExecutionsForPage(SearchSetting search);

    List<Map<String, Object>> findStepsById(String executionId);

    Map<String, Object> findExecutionDetailById(String executionId);

    JobParameters findJobParamsById(String executionId);

    void updateExecution(Long executionId, String executor);
}
