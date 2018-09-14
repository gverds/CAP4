/* 
 * GenericDao.java
 * 
 * Copyright (c) 2011 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.dao;

import java.io.Serializable;
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

    Page<T> findPage(SearchSetting search);

    /**
     * create new search requirement
     * 
     * @return ISearch
     */
    SearchSetting createSearchTemplete();

    <S> S findById(Class<S> clazz, Serializable pk);

    /**
     * find by SearchSetting
     * 
     * @param <S>
     *            bean
     * @param search
     *            SearchSetting
     * @param clazz
     *            Class<S>
     * @return List<S>
     */
    <S> List<S> find(Class<S> clazz, SearchSetting search);

    /**
     * 查詢頁的資料
     * 
     * @param <S>
     *            bean
     * @param clazz
     *            Class<S>
     * @param search
     *            SearchSetting
     * @return Page<S>
     */
    <S> Page<S> findPage(Class<S> clazz, SearchSetting search);

}
