/* 
 * FileDownloadException.java
 * 
 * Copyright (c) 2021 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.exception;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2023年1月4日
 * @author AllenChiu
 * @version
 *          <ul>
 *          <li>2023/1/4,AllenChiu,new
 *          </ul>
 */
public class CapFlowException extends RuntimeException {
    public CapFlowException(String message) {
        super(message);
    }
}
