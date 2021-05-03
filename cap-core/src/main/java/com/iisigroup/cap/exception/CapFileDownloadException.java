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
 * @since 2021年5月3日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2021年5月3日,Lancelot,new
 *          </ul>
 */
public class CapFileDownloadException extends RuntimeException {
    public CapFileDownloadException(String message) {
        super(message);
    }
}
