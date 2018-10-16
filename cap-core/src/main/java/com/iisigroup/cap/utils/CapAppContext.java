/*
 * CapAppContext.java
 *
 * Copyright (c) 2011 International Integrated System, Inc.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.utils;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;

import com.iisigroup.cap.operation.simple.SimpleContextHolder;

/**
 * <pre>
 * ApplicationContext
 * </pre>
 * 
 * @since 2011/11/4
 * @author rodeschen
 * @version
 *          <ul>
 *          <li>2011/11/4,rodeschen,new
 *          <li>2012/12/19,rodeschen,catch NoSuchMessageException
 *          </ul>
 */
public class CapAppContext implements ApplicationContextAware {
    protected final static Logger LOGGER = LoggerFactory.getLogger(CapAppContext.class);

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        synchronized (this) {
            applicationContext = ctx;
        }
    }

    public synchronized static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        return (T) (applicationContext.containsBean(beanName) ? applicationContext.getBean(beanName) : null);
    }

    public static Resource getResource(String path) {
        Resource resource = applicationContext.getResource(path);
        return resource;
    }

    public static <T> T getBean(String beanName, Class<T> c) {
        return (T) applicationContext.getBean(beanName, c);
    }

    public static String getMessage(String key) {
        Locale locale = (Locale) SimpleContextHolder.get(CapWebUtil.LOCALE_KEY);
        return getMessage(key, null, locale == null ? Locale.getDefault() : locale);
    }

    public static String getMessage(String key, Object[] args) {
        Locale locale = (Locale) SimpleContextHolder.get(CapWebUtil.LOCALE_KEY);
        return getMessage(key, args, locale == null ? Locale.getDefault() : locale);
    }

    public static String getMessage(String key, Locale locale) {
        return getMessage(key, null, locale);
    }

    public static String getMessage(String key, Object[] args, Locale locale) {
        try {
            return applicationContext.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.warn("can't find message key:" + key);
            return key;
        }

    }

}
