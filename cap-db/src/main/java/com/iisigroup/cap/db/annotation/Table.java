/* 
 * Table.java
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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2018年9月13日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2018年9月13日,Lancelot,new
 *          </ul>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {
    /**
     * (Optional) The name of the table.
     * <p/>
     * Defaults to the entity name.
     */
    String name() default "";

    /**
     * (Optional) The primary key column of the table. Composite primary key is not allowed.
     * <p/>
     * Defaults to the default primary key column.
     */
    String pkColumn() default "OID";

}
