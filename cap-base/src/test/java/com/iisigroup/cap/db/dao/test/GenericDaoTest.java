/* 
 * GenericDaoTest.java
 * 
 * Copyright (c) 2018 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.dao.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.iisigroup.cap.base.dao.SysParmDao;
import com.iisigroup.cap.base.model.SysParm;
import com.iisigroup.cap.utils.CapDate;
import com.mchange.v2.c3p0.ComboPooledDataSource;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:base-test/applicationContext.xml")
public class GenericDaoTest {
    @Autowired
    private SysParmDao sysParmDao;

    public GenericDaoTest() {
        try {
            SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass("org.h2.Driver");
            dataSource.setJdbcUrl("jdbc:h2:../h2db/ckmsdb");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            builder.bind("java:comp/env/jdbc/capdb", dataSource);
            builder.activate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProvideService() {
        SysParm sysparm = new SysParm();
        sysparm.setParmId("test1");
        sysparm.setParmDesc("測試");
        sysparm.setParmValue("A1234567");
        sysparm.setUpdater("System");
        sysparm.setUpdateTime(CapDate.getCurrentTimestamp());
        sysParmDao.save(sysparm);
        sysparm.setParmValue("A1234568");
        sysParmDao.save(sysparm);
        System.out.println(sysParmDao.count(sysParmDao.createSearchTemplete()));
    }
}
