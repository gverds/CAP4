/* 
 * UUIDIncrementer.java
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.batch.support;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

import com.iisigroup.cap.utils.UUIDGenerator;

/**
 * <pre>
 * UUIDIncrementer
 * </pre>
 * 
 * @since 2012/11/14
 * @author iristu
 * @version
 *          <ul>
 *          <li>2012/11/14,iristu,new
 *          </ul>
 */
public class UUIDIncrementer implements JobParametersIncrementer {

    private final static String RUN_ID = "uuid";

    private String runId = RUN_ID;

    /**
     * The name of the run id in the job parameters. Defaults to "run.id".
     * 
     * @param runId
     */
    public void setRunId(String runId) {
        this.runId = runId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.batch.core.JobParametersIncrementer#getNext(org. springframework.batch.core.JobParameters)
     */
    @Override
    public JobParameters getNext(JobParameters parameters) {
        JobParameters params = (parameters == null) ? new JobParameters() : parameters;
        return new JobParametersBuilder(params).addString(runId, UUIDGenerator.getUUID()).toJobParameters();
    }
}
