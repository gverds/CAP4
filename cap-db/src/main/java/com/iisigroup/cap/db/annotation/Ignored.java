/* 
 * Ignore.java
 * 
 * Copyright (c) 2018 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <pre>
 * Mark a filed in model as ignored one.
 * </pre>
 * 
 * @since 2018年9月14日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2018年9月14日,Lancelot,new
 *          </ul>
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Ignored {

}
