/* 
 * CapBeanUtilTest.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.utils;

import java.sql.Clob;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.iisigroup.cap.model.GenericBean;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2019年8月5日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2019年8月5日,Lancelot,new
 *          </ul>
 */
public class CapBeanUtilTest {
    @Test
    public void testMap2Bean() {
        TestBean bean = new TestBean();
        Map<String, Object> map = new HashMap<>();
        map.put("string", "123");
        map.put("time", "2019/08/05");
        CapBeanUtil.map2Bean(map, bean);
        System.out.println(bean.getString());
        System.out.println(bean.getTime());
        map.put("string", "1223");
        map.put("time", "2019/08/05 11:22:33");
        map.put("clob", "AAA");
        CapBeanUtil.map2Bean(map, bean);
        System.out.println(bean.getString());
        System.out.println(bean.getTime());
        System.out.println(bean.getClob());
    }

    class TestBean extends GenericBean {
        private String string;
        private Timestamp time;
        private Clob clob;

        /**
         * @return the string
         */
        public String getString() {
            return string;
        }

        /**
         * @param string
         *            the string to set
         */
        public void setString(String string) {
            this.string = string;
        }

        /**
         * @return the time
         */
        public Timestamp getTime() {
            return time;
        }

        /**
         * @param time
         *            the time to set
         */
        public void setTime(Timestamp time) {
            this.time = time;
        }

        /**
         * @return the clob
         */
        public Clob getClob() {
            return clob;
        }

        /**
         * @param clob the clob to set
         */
        public void setClob(Clob clob) {
            this.clob = clob;
        }
        
        
    }
}
