/* 
 * CapDBException.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.exception;

import com.iisigroup.cap.exception.CapException;

/**
 * <pre>
 * CapDBException
 * </pre>
 * 
 * @since 2012/9/21
 * @author iristu
 * @version
 *          <ul>
 *          <li>2012/9/21,iristu,new
 *          </ul>
 */
public class CapDBException extends CapException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new cap exception.
     */
    public CapDBException() {
        super();
    }

    /**
     * Instantiates a new cap exception.
     * 
     * @param message
     *            the message
     * @param causeClass
     *            the cause class
     */
    public CapDBException(String message, Class<?> causeClass) {
        super(message, causeClass);
    }

    /**
     * Instantiates a new cap exception.
     * 
     * @param cause
     *            the cause
     * @param causeClass
     *            the cause class
     */
    public CapDBException(Throwable cause, Class<?> causeClass) {
        super(cause, causeClass);
    }

    /**
     * Instantiates a new cap exception.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param causeClass
     *            the cause class
     */
    public CapDBException(String message, Throwable cause, Class<?> causeClass) {
        super(message, cause, causeClass);
    }

}
