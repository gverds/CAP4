/* 
 * CapNamingStrategy.java
 * 
 * Copyright (c) 2009-2014 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.jdbc.support;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * <pre>
 * Custom Naming Strategy.
 * Hibernate 6 migration: ImprovedNamingStrategy was removed; now implements
 * PhysicalNamingStrategyStandardImpl which passes through names unchanged
 * (preserves camelCase, no underscore conversion).
 * Note: Spring XML config must reference this class via
 * hibernate.physical_naming_strategy property instead of hibernate.ejb.naming_strategy.
 * </pre>
 * 
 * @since 2014/3/31
 * @author Sunkist Wang
 * @version
 *          <ul>
 *          <li>2014/3/31,Sunkist Wang,new
 *          <li>2026/04/21,migration,Hibernate 6: extend
 *          PhysicalNamingStrategyStandardImpl
 *          </ul>
 */
public class CapNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    /***/
    private static final long serialVersionUID = 1L;

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName;
    }
}
