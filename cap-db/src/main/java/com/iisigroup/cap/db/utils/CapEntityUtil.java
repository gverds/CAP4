/* 
 * CapEntityUtil.java
 * 
 * Copyright (c) 2011 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.iisigroup.cap.db.annotation.Ignored;

/**
 * <pre>
 * Openjpa entity util
 * </pre>
 * 
 * @since 2011年8月19日
 * @author iristu
 * @version
 *          <ul>
 *          <li>2011年8月19日,iristu,new
 *          </ul>
 */
public class CapEntityUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapEntityUtil.class);

    /**
     * 取得傳入entity所有欄位名稱
     * 
     * @param <T>
     *            entity
     * @param entity
     *            jpa entity
     * @return String[]
     */
    public static <T> String[] getColumnName(T entity) {
        Set<Class<? extends Annotation>> ignore = new HashSet<Class<? extends Annotation>>();
        ignore.add(Ignored.class);
        return getColumnName(entity, ignore);
    }

    public static <T> String[] getColumnName(T entity, boolean constantize) {
        Set<Class<? extends Annotation>> ignore = new HashSet<Class<? extends Annotation>>();
        ignore.add(Ignored.class);
        return getColumnName(entity, ignore, constantize);
    }

    public static <T> String[] getColumnName(T entity, Set<Class<? extends Annotation>> ignoreAnnotation) {
        return getColumnName(entity, ignoreAnnotation, false);
    }

    /**
     * 取得傳入 entity 的 column name
     * 
     * @param entity
     * @param ignoreAnnotation
     * @param constantize
     *            轉成常數命名方式(全大寫，以底線分隔)
     * @return
     */
    public static <T> String[] getColumnName(T entity, Set<Class<? extends Annotation>> ignoreAnnotation, boolean constantize) {
        Set<String> cols = new LinkedHashSet<String>();
        try {
            Class searchClazz = getEntityClass(entity);
            while (!Object.class.equals(searchClazz) && searchClazz != null) {
                Field[] fields = searchClazz.getDeclaredFields();
                f: for (Field field : fields) {
                    if (ignoreAnnotation != null) {
                        for (Class<? extends Annotation> ignore : ignoreAnnotation) {
                            if (field.isAnnotationPresent(ignore)) {
                                continue f;
                            }
                        }
                    }
                    if (!Modifier.isStatic(field.getModifiers())) {
                        if (constantize) {
                            cols.add(underscoreName(field.getName()));
                        } else {
                            cols.add(field.getName());
                        }
                    }
                }
                searchClazz = searchClazz.getSuperclass();
            }
            return cols.toArray(new String[cols.size()]);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * 取得傳入entity所有欄位名稱及型態
     * 
     * @param <T>
     *            entity
     * @param entity
     *            jpa entity
     * @return Map<String, Class>
     */
    @SuppressWarnings("rawtypes")
    public static <T> Map<String, Class> getColumnType(T entity) {
        Map<String, Class> cols = new HashMap<String, Class>();
        try {
            Class searchClazz = getEntityClass(entity);
            while (!Object.class.equals(searchClazz) && searchClazz != null) {
                Field[] fields = searchClazz.getDeclaredFields();
                for (Field field : fields) {
                    cols.put(field.getName(), field.getType());
                }
                searchClazz = searchClazz.getSuperclass();
            }
            return cols;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * 取得entity class
     * 
     * @param <T>
     *            entity
     * @param entity
     *            openjpa entity
     * @return Class
     * @throws ClassNotFoundException
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Class<T> getEntityClass(T entity) throws ClassNotFoundException {
        return (Class<T>) entity.getClass();
    }

    public static String underscoreName(String name) {
        if (!StringUtils.hasLength(name)) {
            return "";
        }
        boolean process = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isUpperCase(c) && '_' != c && '.' != c) {
                process = true;
                break;
            }
        }
        if (process) {
            StringBuilder result = new StringBuilder();
            result.append(lowerCaseName(name.substring(0, 1)));
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                String slc = lowerCaseName(s);
                if (!s.equals(slc)) {
                    result.append("_").append(slc);
                } else {
                    result.append(s);
                }
            }
            return result.toString().toUpperCase(Locale.US);
        } else {
            return name;
        }
    }

    public static String lowerCaseName(String name) {
        return name.toLowerCase(Locale.US);
    }

}
