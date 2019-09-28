/* 
 * GenericDao.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.dao;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.iisigroup.cap.db.model.Page;

/**
 * <p>
 * interface IGenericDao.
 * </p>
 * 
 * @author iristu
 * @version
 *          <ul>
 *          <li>2010/7/7,iristu,new
 *          <li>2011/6/20,iristu,增加findPage
 *          <li>2011/11/1,rodeschen,from cap
 *          </ul>
 * @param <T>
 *            the model
 */
public interface GenericDao<T> {

    void save(Object models);

    /**
     * Insert.
     * 
     * @param models
     *            the entity
     */
    void save(List<?> models);

    /**
     * Delete.
     * 
     * @param entity
     *            the entity
     */
    void delete(Object entity);

    void delete(List<?> entries);

    /**
     * Find.
     * 
     * @param pk
     *            the oid
     * 
     * @return the t
     */
    T find(Serializable pk);

    <S> List<S> find(Class<S> clazz, SearchSetting search);

    T find(T entity);

    T findUniqueOrNone(SearchSetting search);

    List<T> find(SearchSetting search);

    /**
     * Count.
     * 
     * @param search
     *            SearchSetting
     * @return the int
     */
    int count(SearchSetting search);

    Iterator<T> list(int first, int count);

    Page<T> findPage(SearchSetting search);

    <S> Page<S> findPage(Class<S> clazz, SearchSetting search);

    /**
     * create new search requirement
     * 
     * @return ISearch
     */
    SearchSetting createSearchTemplete();

    /**
     * flush
     */
    void flush();

    <S> S findById(Class<S> clazz, Serializable pk);

}
