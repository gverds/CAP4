/*
 * EloanUtils.java
 *
 * Copyright (c) 2019 International Integrated System, Inc.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System, Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.tcb.ecol.core.utils;

import java.beans.PropertyDescriptor;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.Column;
import javax.persistence.Table;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.Result;
import com.iisigroup.cap.component.impl.AjaxFormResult;
import com.iisigroup.cap.db.constants.SearchMode;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.model.DataObject;
import com.iisigroup.cap.db.model.Page;
import com.iisigroup.cap.db.model.SearchModeParameter;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.exception.CapMessageException;
import com.iisigroup.cap.formatter.Formatter;
import com.iisigroup.cap.model.GenericBean;
import com.iisigroup.cap.security.CapSecurityContext;
import com.iisigroup.cap.utils.CapBeanUtil;
import com.iisigroup.cap.utils.CapDate;
import com.iisigroup.cap.utils.CapMath;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.GsonUtil;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;;

/**
 * <pre>
 * EloanUtils
 * </pre>
 *
 * @author 1509018
 * @version
 *          <ul>
 *          <li>2019年10月22日,1509018,new
 *          </ul>
 * @since 2019年10月22日
 */
public class EloanUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapBeanUtil.class);
    private static String[] selmaUiCase = { "regTel", "resTel", "banTel", "regAddress", "resAddress", "banAddress", "idn", "ban", "location" };
    public static Map<String, String> custInfoColMap = getCustInfoColMap();
    public static Map<String, String> guarInfoColMap = getGuarInfoColMap();
    public static Map<String, String> relInfoColMap = getRelInfoColMap();
    public static Map<String, String> lawRecColMap = getLawRecColMap();
    public static Map<String, String> lawRelDtlColMap = getLawRelDtlColMap();
    public static Map<String, String> lawAmtDtlColMap = getLawAmtDtlColMap();
    public static Map<String, String> lawCertDtlColMap = getLawCertDtlColMap();
    public static Map<String, String> lawCauseColMap = getLawCauseColMap();
    public static Map<String, String> lawCorrectColMap = getLawCorrectColMap();
    public static Map<String, String> lawDepositColMap = getLawDepositColMap();
    public static Map<String, String> lawCertColMap = getLawCertColMap();
    public static Map<String, String> lawAccuseColMap = getLawAccuseColMap();
    public static Map<String, String> lawDistrainColMap = getLawDistrainColMap();
    public static Map<String, String> lawDistrainPropColMap = getLawDistrainPropColMap();
    public static Map<String, String> lawEnforceColMap = getLawEnforceColMap();
    public static Map<String, String> lawEnforcePropColMap = getLawEnforcePropColMap();

    /**
     * 登入角色昰否為主管
     *
     * @return
     */
    public static boolean isSupv() {
        boolean result = false;
        Set<String> roleIds = CapSecurityContext.getRoleIds();
        if (roleIds != null && roleIds.size() > 0) {
            for (String roleId : roleIds) {
                if (roleId.length() >= 2 && roleId.charAt(1) != '1') {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * 取012轉成ABC
     *
     * @param no
     * @return
     */
    public static String caseNo(int no) {
        int i = 65 + Math.abs(no);
        return Character.toString((char) i);
    }

    public static <T extends GenericBean> T checkModelValue(T bean) {
        Field[] cols = CapBeanUtil.getField(bean.getClass(), true);
        Table table = bean.getClass().getAnnotation(Table.class);
        for (Field f : cols) {
            Column column = f.getAnnotation(Column.class);
            // rule
            Object value = bean.get(f.getName());
            if (value != null && value.toString().length() > column.length()) {
                throw new CapMessageException("欄位輸入的值太長：" + table.name() + "." + column.name(), bean.getClass());
            }
        }
        return bean;
    }

    public static <T extends GenericBean> Map<String, Object> getModelColName(Class<T> clazz) {
        Field[] cols = CapBeanUtil.getField(clazz, true);
        Map<String, Object> map = new HashMap<>();
        for (Field f : cols) {
            Column column = f.getAnnotation(Column.class);
            if (column != null) {
                map.put(column.name(), null);
            }
        }
        return map;
    }

    /**
     * 將List<GenericBean>轉成List<Map<String, Object>>
     *
     * @param entryList
     * @param clazz
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> List<Map<String, Object>> listBean2Map(List<S> entryList, Class<T> clazz) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (S bean : entryList) {
            list.add(cleanMap(CapBeanUtil.bean2Map(bean, CapBeanUtil.getFieldName(clazz, true))));
        }
        return list;
    }

    /**
     * 將List<GenericBean>轉成List<Map<String, Object>>
     *
     * @param entryList
     * @param clazz
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> List<Map<String, Object>> listBean2Map(List<S> entryList, String[] columns) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (S bean : entryList) {
            list.add(cleanMap(CapBeanUtil.bean2Map(bean, columns)));
        }
        return list;
    }

    /**
     * 類似putAll，但是已存在的值不會被覆蓋
     *
     * @param target
     * @param source
     * @return
     */
    public static Map<String, Object> putMapOnlyEmpty(Map<String, Object> target, Map<String, Object> source) {
        for (String key : source.keySet()) {
            if (target.get(key) == null) {
                target.put(key, source.get(key));
            }
        }
        return target;
    }

    /**
     * 將Page<GenericBean>轉成Page<Map<String, Object>>
     *
     * @param pageBean
     * @param clazz
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> Page<Map<String, Object>> pageBean2Map(Page<S> pageBean, Class<T> clazz) {
        return new Page<Map<String, Object>>(listBean2Map(pageBean.getContent(), clazz), pageBean.getTotalRow(), pageBean.getPageSize(), pageBean.getPageNumber());
    }

    /**
     * CapBeanUtil的bean2Map，簡化String[]的欄位填入，直接使用.class，同時去掉null的key
     *
     * @param source
     * @param clazz
     * @return
     */
    public static <T extends GenericBean> Map<String, Object> bean2Map(T source, Class<T> clazz) {
        return cleanMap(CapBeanUtil.bean2Map(source, CapBeanUtil.getFieldName(clazz, true)));
    }

    /**
     * @param <T>
     * @param source
     * @return
     */
    public static <T extends GenericBean> Map<String, Object> bean2Map(T source) {
        if (source != null) {
            return cleanMap(CapBeanUtil.bean2Map(source, CapBeanUtil.getFieldName(source.getClass(), true)));
        } else {
            return new HashMap<String, Object>();
        }
    }

    public static <T extends GenericBean> Map<String, Object> bean2MapWithFix(T source, String fixString, boolean isPrefix) {
        if (source != null) {
            return cleanMap(CapBeanUtil.bean2Map(source, CapBeanUtil.getFieldName(source.getClass(), true), fixString, isPrefix));
        } else {
            return new HashMap<String, Object>();
        }
    }

    /**
     * @param <T>
     * @param source
     * @param clazz
     * @return
     */
    public static <T extends GenericBean> Map<String, Object> bean2Result(T source, Class<T> clazz) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (source != null) {
            map.putAll(cleanMap(CapBeanUtil.bean2Map(source, CapBeanUtil.getFieldName(clazz, true))));
        }
        return map;
    }

    /**
     * 將bean轉成可加prefix的result，順便也把共用元件一並轉換
     *
     * @param <T>
     * @param source
     * @param clazz
     * @param prefix
     * @return
     */
    public static <T extends GenericBean> AjaxFormResult bean2Res(T source, Class<T> clazz, String prefix) {
        AjaxFormResult result = new AjaxFormResult();
        Map<String, Object> map = bean2Map(source, clazz);
        prefix = prefix == null ? "" : prefix;
        // 共用元件處理
        List<String> uiRun = new ArrayList<String>();

        for (String s : selmaUiCase) {
            for (String col : CapBeanUtil.getFieldName(clazz, true)) {
                if (col.indexOf(s.substring(0, 3)) == 0 && uiRun.indexOf(s) == -1) {
                    uiRun.add(s);
                }
            }
        }
        // 規則: UI的key值就是prefix+下面case的名稱
        // ex:
        // prefix="test_"
        // 戶籍地址的id="test_regAddress"
        for (String col : uiRun) {
            switch (col) {
            case "regTel":
            case "resTel":
            case "banTel":
                map = CommonUiUtil.getTelBeanMap2UiMap(map, prefix + col, col);
                break;
            case "regAddress":
            case "resAddress":
            case "banAddress":
                map = CommonUiUtil.getAddressBeanMap2UiMap(map, prefix + col, col);
                break;
            case "idn":
            case "ban":
                map = CommonUiUtil.getIdnBeanMap2UiMap(map, prefix + col, col);
                break;
            }
        }
        result.putAll(map);
        return result;
    }

    /**
     * 將request轉成map，順便處理共用元件jsonStr
     *
     * @param request
     * @param prefix
     * @return
     */
    public static Map<String, Object> req2Map(Request request, String prefix) {
        Map<String, Object> oldMap = req2Map(request);
        Map<String, Object> newMap = new HashMap<String, Object>();
        for (String key : oldMap.keySet()) {
            Object value = oldMap.get(key);
            if (prefix != null && !CapString.isEmpty(prefix) && key.indexOf(prefix) == 0) {
                key = key.replaceFirst(prefix, "");
            }
            newMap.put(key, value);
            for (String s : selmaUiCase) {
                if (key.equals(s)) {
                    newMap.remove(key);
                    switch (key) {
                    case "location":
                        newMap.putAll(CommonUiUtil.getLocationUiStr2BeanMap(value.toString(), key));
                        break;
                    case "regTel":
                    case "resTel":
                    case "banTel":
                        newMap.putAll(CommonUiUtil.getTelUiStr2BeanMap(value.toString(), key));
                        break;
                    case "regAddress":
                    case "resAddress":
                    case "banAddress":
                        newMap.putAll(CommonUiUtil.getAddressUiStr2BeanMap(value.toString(), key));
                        break;
                    case "idn":
                    case "ban":
                        newMap.putAll(CommonUiUtil.getIdnUiStr2BeanMap(value.toString(), key));
                        break;
                    }
                }
            }
        }
        return newMap;
    }

    /**
     * 將多個Bean轉換成result，但是重複的的key將不會被確保TODO(未完成)
     *
     * @param souceMap
     *            Map<Class<T>, GenericBean>放入要轉換的Bean
     * @param prefix
     * @return
     */
    public static <T extends GenericBean> Result beans2Res(Map<Class<T>, GenericBean> souceMap, String prefix) {
        AjaxFormResult result = new AjaxFormResult();
        Map<String, Object> map = new HashMap<String, Object>();
        for (Class<T> clazz : souceMap.keySet()) {
            map.putAll(cleanMap(CapBeanUtil.bean2Map(souceMap.get(clazz), CapBeanUtil.getFieldName(clazz, true))));
        }
        return result;
    }

    /**
     * @param list
     * @return
     */
    public static String listmap2ListJsonString(List<Map<String, Object>> list) {

        List<JSONObject> jsonObj = new ArrayList<JSONObject>();

        for (Map<String, Object> data : list) {
            JSONObject obj = new JSONObject(data);
            jsonObj.add(obj);
        }
        return new JSONArray(jsonObj).toString();
    }

    public static String listmapString2ListJsonString(List<Map<String, String>> list) {

        List<JSONObject> jsonObj = new ArrayList<JSONObject>();

        for (Map<String, String> data : list) {
            JSONObject obj = new JSONObject(data);
            jsonObj.add(obj);
        }
        return new JSONArray(jsonObj).toString();
    }

    public static String setmapString2ListJsonString(Set<Map<String, Object>> list) {
        List<JSONObject> jsonObj = new ArrayList<>();
        for (Map<String, Object> data : list) {
            JSONObject obj = new JSONObject(data);
            jsonObj.add(obj);
        }
        return new JSONArray(jsonObj).toString();
    }

    public static <T extends GenericBean> SearchSetting seachEqualsByRequest(SearchSetting search, Request request, Class<T> clazz) {
        for (String key : request.keySet()) {
            if (Arrays.asList(CapBeanUtil.getFieldName(clazz, true)).indexOf(key) != -1) {
                search.addSearchModeParameters(SearchMode.EQUALS, key, request.get(key));
            }
        }
        return search;
    }

    /**
     * @param <T>
     * @param listMap
     * @param clazz
     * @return
     */
    public static <T extends GenericBean> List<T> listMap2ListBean(List<Map<String, Object>> listMap, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        try {
            for (Map<String, Object> map : listMap) {
                result.add(map2Bean(map, clazz.newInstance()));
            }
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return result;
    }

    /**
     * @param jsonString
     * @return
     */
    public static Map<String, Object> jsonString2Map(String jsonString) {
        Gson gson = new Gson();
        Type resultType = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> result = gson.fromJson(jsonString, resultType);
        return result;
    }

    /**
     * @param listJsonString
     * @return
     */
    public static List<Map<String, Object>> listJsonString2ListMap(String listJsonString) {
        Gson gson = new Gson();
        Type resultType = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> result = gson.fromJson(listJsonString, resultType);
        return result;
    }

    /**
     * @param listJsonString
     * @return
     */
    public static Set<Map<String, Object>> setJsonString2SetMap(String setJsonString) {
        Gson gson = new Gson();
        Type resultType = new TypeToken<Set<Map<String, Object>>>() {
        }.getType();
        Set<Map<String, Object>> result = gson.fromJson(setJsonString, resultType);
        return result;
    }

    /**
     * 將A model轉成map，再轉進B model
     *
     * @param source
     * @param sourceClass
     * @param target
     * @return
     */
    public static <S extends GenericBean, SC extends GenericBean, T extends GenericBean> T bean2Map2bean(S source, Class<SC> sourceClass, T target) {
        Map<String, Object> map = CapBeanUtil.bean2Map(source, CapBeanUtil.getFieldName(sourceClass, false));
        return map2Bean(map, target);
    }

    /**
     * 將A model有值的部分，新增到B model
     *
     * @param source
     * @param target
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> T beanUpdate2bean(S source, T target) {
        Map<String, Object> smap = CapBeanUtil.bean2Map(source, CapBeanUtil.getFieldName(source.getClass(), false));
        Map<String, Object> tmap = CapBeanUtil.bean2Map(target, CapBeanUtil.getFieldName(target.getClass(), false));
        smap.put("oid", tmap.get("oid"));
        smap.put("mid", tmap.get("mid"));
        smap.put("pid", tmap.get("pid"));
        return map2GenericBean(smap, target);
    }

    /**
     * 將欄位名稱為下底線的Map轉成Bean
     *
     * @param map
     * @param entry
     * @return
     */
    public static <T extends GenericBean> T underLineMap2Bean(Map<String, Object> map, T entry) {
        if (map != null && !map.isEmpty()) {
            Field[] cols = CapBeanUtil.getField(entry.getClass(), true);
            for (Field f : cols) {
                String colName = f.getName();
                StringBuilder sb = new StringBuilder();
                int count = 0;
                sb.append(colName);
                for (int i = 0; i < colName.length(); i++) {
                    if (Character.isUpperCase(colName.charAt(i))) {
                        sb.insert(i + count, "_");
                        count += 1;
                    }
                }
                String keyName = sb.toString().toUpperCase();
                if (map.containsKey(keyName)) {
                    Object value;
                    value = map.get(keyName);
                    if (value != null && value.getClass().getName().equals("java.sql.Timestamp")) {
                        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        value = sdf.format(value);
                    }
                    CapBeanUtil.setField(entry, f.getName(), value);
                }
            }
        }
        return entry;
    }

    /**
     * 將map值放入bean中
     *
     * @param prefix
     * @param map
     *            {fieldId=fieldValue}
     * @param entry
     *            the bean
     * @param cols
     *            columns
     * @return
     */
    public static <T extends GenericBean> T map2Bean(String prefix, Map<String, Object> map, T entry, String[] columns) {
        if (map != null && !map.isEmpty()) {
            for (String f : columns) {
                String key = prefix + f;
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    setField(entry, f, value);
                }
            }
        }
        return entry;
    }

    /**
     * @param prefix
     * @param map
     * @param entry
     * @param columns
     * @return
     */
    public static <T extends GenericBean> T map2GenericBean(Map<String, Object> map, T entry) {
        if (map != null && !map.isEmpty()) {
            Field[] cols = getField(entry.getClass(), true);
            for (Field f : cols) {
                String key = f.getName();
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    setField(entry, key, value);
                }
            }
        }
        return entry;
    }

    /**
     * 將List中的map內為空值的key刪掉
     *
     * @param list
     * @return
     */
    public static List<Map<String, Object>> cleanListMap(List<Map<String, Object>> list) {
        List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : list) {
            cleanMap(map);
            newList.add(cleanMap(map));
        }
        return newList;
    }

    /**
     * @param <T>
     * @param request
     * @param entry
     * @param prefix
     * @return
     */
    public static <T extends GenericBean> T req2Bean(Request request, T entry, String prefix) {
        prefix = prefix == null ? "" : prefix;
        return map2Bean(prefix, req2Map(request, prefix), entry);
    }

    /**
     * 將request轉成map，map裡的String-Object，實際上是String-String
     *
     * @param request
     * @return
     */
    public static Map<String, Object> req2Map(Request request) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> set = new HashSet<String>();
        set.addAll(request.keySet());
        for (String key : set) {
            String value = request.get(key);
            if (value == null || CapString.isEmpty(value)) {
                map.put(key, null);
            } else {
                map.put(key, value);
            }
        }
        return cleanMap(map);
    }

    public static Map<String, Object> req2Map2(Request request) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> set = new HashSet<String>();
        set.addAll(request.keySet());
        for (String key : set) {
            String value = request.get(key);
            if (value == null) {
                map.put(key, null);
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 將request轉成map，map裡的String-Object，實際上是String-String
     *
     * @param request
     * @return
     */
    public static <T extends GenericBean> Map<String, Object> req2Map(Request request, Class<T> clazz) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> set = new HashSet<String>();
        set.addAll(request.keySet());
        for (String key : CapBeanUtil.getFieldName(clazz, true)) {
            String value = request.get(key);
            if (value == null || CapString.isEmpty(value)) {
                // request.remove(key);
            } else {
                map.put(key, value);
            }
        }
        return cleanMap(map);
    }

    /**
     * 將map內為空字串的value轉為null
     *
     * @param map
     * @return
     */
    public static Map<String, Object> cleanMap(Map<String, Object> map) {
        Set<String> set = new HashSet<String>();
        set.addAll(map.keySet());
        for (String key : set) {
            Object val = map.get(key);
            if (val == null || val.getClass().toString().equals("class java.lang.String")) {
                if (CapString.isEmpty((String) val) || val.equals("null")) {
                    map.put(key, null);
                }
            }
        }
        return map;
    }

    /**
     * 將map內為空值的key刪掉
     *
     * @param map
     * @return
     */
    public static Map<String, Object> cleanMapByRemove(Map<String, Object> map) {
        Set<String> set = new HashSet<String>();
        set.addAll(map.keySet());
        for (String key : set) {
            Object val = map.get(key);
            if (val == null || val.getClass().toString().equals("class java.lang.String")) {
                if (CapString.isEmpty((String) val) || val.equals("null")) {
                    map.remove(key);
                }
            }
        }
        return map;
    }

    private static final int alphaStep = 'a' - 'A';

    /**
     * @param c
     * @return
     */
    public static char toUpperCase(char c) {
        return isLowerCase(c) ? (char) (c - alphaStep) : c;
    }

    /**
     * @param c
     * @return
     */
    public static boolean isUpperCase(char c) {
        return c >= 'A' && c <= 'Z';
    }

    /**
     * @param c
     * @return
     */
    public static boolean isLowerCase(char c) {
        return c >= 'a' && c <= 'z';
    }

    /**
     * @param camel
     * @return
     */
    public static String camel2UnderLine(String camel) {
        StringBuilder underline = new StringBuilder();
        char[] chars = camel.trim().toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i == 0)
                underline.append(toUpperCase(c));
            else {
                if (isUpperCase(c)) {
                    underline.append('_');
                    underline.append(c);
                } else
                    underline.append(toUpperCase(c));
            } /* from w ww .ja va 2 s. c o m */
        }

        return underline.toString();
    }

    public static Map<String, Object> underLine2CamelFstsmall(Map<String, Object> map) {
        map = map == null ? new HashMap<String, Object>() : underLine2CamelF(map);
        Map<String, Object> newMap = new HashMap<String, Object>();
        for (String key : map.keySet()) {
            String newKey = key.substring(0, 1).toLowerCase() + key.substring(1, key.length());
            newMap.put(newKey, map.get(key));
        }
        return newMap;
    }

    public static String underLine2CamelFstsmall(String key) {
        String[] kArr = key.toLowerCase().split("_");
        String newKey = "";
        for (int i = 0; i < kArr.length; i++) {
            if (i == 0) {
                newKey += kArr[i];
            } else {
                newKey += kArr[i].substring(0, 1).toUpperCase() + kArr[i].substring(1, kArr[i].length());
            }
        }
        return newKey.substring(0, 1).toLowerCase() + newKey.substring(1, newKey.length());
    }

    /**
     * 將內的key值，全部轉換成Camel
     *
     * @param request
     * @param equlasKeys
     * @return
     * @throws Exception
     */
    public static Map<String, Object> underLine2Camel(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<String, Object>();
        for (String key : map.keySet()) {
            boolean isCamel = false;
            for (int i = 0; i < key.length(); i++) {
                char chr = key.charAt(i);
                if (Character.isLowerCase(chr)) {
                    isCamel = true;
                    break;
                }
            }
            if (isCamel) {
                if (key.indexOf("_") != -1) {
                    // throw new RuntimeException("key: " + key + " is not just underLine or camel string");
                } else {
                    newMap.put(key, map.get(key));
                }
            } else {
                String[] kArr = key.toLowerCase().split("_");
                String newKey = "";
                for (int i = 0; i < kArr.length; i++) {
                    if (i == 0) {
                        newKey += kArr[i];
                    } else {
                        newKey += kArr[i].substring(0, 1).toUpperCase() + kArr[i].substring(1, kArr[i].length());
                    }
                }
                newMap.put(newKey, map.get(key));
            }
        }
        return newMap;
    }

    public static Map<String, Object> underLine2CamelF(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<String, Object>();
        for (String key : map.keySet()) {
            String[] kArr = key.toLowerCase().split("_");
            String newKey = "";
            for (int i = 0; i < kArr.length; i++) {
                if (i == 0) {
                    newKey += kArr[i];
                } else {
                    newKey += kArr[i].substring(0, 1).toUpperCase() + kArr[i].substring(1, kArr[i].length());
                }
            }
            newMap.put(newKey, map.get(key));
        }
        return newMap;
    }

    /**
     * <pre>
     * 將map值放入bean中
     * </pre>
     *
     * @param <T>
     *            T extends GenericBean
     * @param map
     *            {fieldId=fieldValue}
     * @param entry
     *            bean
     * @return T
     */
    public static <T extends GenericBean> T map2Bean(Map<String, Object> map, T entry) {
        return map2Bean("", map, entry);
    }

    /**
     * <pre>
     * 將map值放入bean中
     * </pre>
     *
     * @param <T>
     *            T extends GenericBean
     * @param prefix
     *            fieldId的前綴值
     * @param map
     *            {fieldId=fieldValue}
     * @param entry
     *            the bean
     * @return T
     */
    public static <T extends GenericBean> T map2Bean(String prefix, Map<String, Object> map, T entry) {
        return map2Bean(prefix, map, entry, entry.getClass());
    }

    /**
     * <pre>
     * 將map值放入bean中
     * </pre>
     *
     * @param <T>
     *            T extends GenericBean
     * @param map
     *            {fieldId=fieldValue}
     * @param entry
     *            bean
     * @param clazz
     *            Class<T>
     * @return T
     */
    public static <T extends GenericBean> T map2Bean(Map<String, Object> map, T entry, Class<T> clazz) {
        return map2Bean("", map, entry, clazz);
    }

    /**
     * <pre>
     * 將map值放入bean中，去掉oid
     * </pre>
     *
     * @param prefix
     * @param map
     * @param entry
     * @return
     */
    public static <T extends GenericBean> T map2NewBean(String prefix, Map<String, Object> map, T entry) {
        prefix = prefix == null ? "" : prefix;
        if (map != null && !map.isEmpty()) {
            if (prefix.indexOf("_") == -1) {
                map = underLine2Camel(map);
            }
            map.remove("oid");
            Field[] cols = getField(entry.getClass(), true);
            for (Field f : cols) {
                String key = prefix + f.getName();
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    if (value instanceof String && StringUtils.equals(value.toString(), "null")) {
                        value = "";
                    }
                    setField(entry, f.getName(), value);
                }
            }
        }
        return entry;
    }

    public static Map<String, Integer> beanFieldType2Map(Class<?> clazz) {
        Map<String, Integer> map = new HashMap<>();
        Field[] cols = getField(clazz, true);
        for (Field field : cols) {
            int i = 0;
            Column column = field.getAnnotation(Column.class);
            String colName = column.columnDefinition();
            if (colName.indexOf("(") != -1) {
                colName = colName.split("(")[0];
            }
            switch (colName) {
            case "NUMBER":
                i = Types.DECIMAL;
                break;
            case "VARCHAR":
            case "VARCHAR2":
                i = Types.VARCHAR;
                break;
            }
            map.put(field.getName(), i);
        }
        return map;
    }

    /**
     * <pre>
     * 將map值放入bean中
     * </pre>
     *
     * @param <T>
     *            T extends GenericBean
     * @param prefix
     *            fieldId的前綴值
     * @param map
     *            {fieldId=fieldValue}
     * @param entry
     *            the bean
     * @param clazz
     *            the bean class
     * @return T
     */
    public static <T extends GenericBean> T map2Bean(String prefix, Map<String, Object> map, T entry, Class<?> clazz) {
        prefix = prefix == null ? "" : prefix;
        if (map != null && !map.isEmpty()) {
            if (prefix.indexOf("_") == -1) {
                map = underLine2Camel(map);
            }
            Field[] cols = getField(clazz, true);
            for (Field f : cols) {
                String key = prefix + f.getName();
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    setField(entry, f.getName(), value);
                }
            }
        }
        return entry;
    }

    public static <T extends GenericBean> T map2BeanNullStrIsNullAnd2Camel(Map<String, Object> map, T entry) {
        Class<?> clazz = entry.getClass();
        if (map != null && !map.isEmpty()) {
            map = underLine2Camel(map);
            Field[] cols = getField(clazz, true);
            for (Field f : cols) {
                String key = f.getName();
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    if ("NULL".equalsIgnoreCase((String) value)) {
                        value = null;
                    }
                    setField(entry, f.getName(), value);
                }
            }
        }
        return entry;
    }

    /**
     * 將有更新的資料放進Bean裡面，null不會覆蓋已經有值的
     *
     * @param prefix
     * @param map
     * @param entry
     * @param clazz
     * @return
     */
    public static <T extends GenericBean> T addmap2Bean(Map<String, Object> map, T entry) {
        if (map != null && !map.isEmpty()) {
            Field[] cols = getField(entry.getClass(), true);
            for (Field f : cols) {
                String key = f.getName();
                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    if (value != null) {
                        setField(entry, f.getName(), value);
                    }
                }
            }
        }
        return entry;
    }

    /**
     * 將有更新的資料放進Bean裡面，null不會覆蓋已經有值的
     *
     * @param source
     * @param destination
     * @return
     */
    public static <T extends GenericBean> T addBean2Bean(T source, T destination) {
        Field[] cols = getField(source.getClass(), true);
        for (Field f : cols) {
            Object value = source.get(f.getName());
            if (value != null) {
                if (new Gson().toJson(value).equals("\"null\"")) {
                    value = null;
                }
                setField(destination, f.getName(), value);
            }
        }
        return destination;
    }

    public static <T extends GenericBean> T bigDecFitDBRule(T models) {
        Table table = models.getClass().getAnnotation(Table.class);
        for (Field f : getField(models.getClass(), false)) {
            Column column = f.getAnnotation(Column.class);
            // NUMBER的欄位處理
            if (column != null && column.columnDefinition().indexOf("NUMBER") != -1) {
                int p = column.precision();
                int s = column.scale();
                BigDecimal bd = (BigDecimal) models.get(f.getName());
                if (bd != null) {
                    if (bd.compareTo(BigDecimal.ZERO) != 0) {
                        bd = bd.divide(BigDecimal.ONE, s, BigDecimal.ROUND_HALF_UP);
                    }
                    models.set(f.getName(), bd);
                    int len;
                    if (bd.toString().indexOf(".") != -1) {
                        len = bd.toString().split("\\.")[0].length();
                    } else {
                        len = bd.toString().length();
                    }
                    // if (len > p || bd.compareTo(BigDecimal.ZERO) == -1) {
                    if (len > p) {
                        throw new CapMessageException("極端值錯誤!!<br>" + table.name() + "." + column.name() + "(" + column.columnDefinition() + ")不符合實際值(" + bd.toString() + ")", models.getClass());
                    }
                }
            }
        }
        return models;
    }

    /**
     * 取得傳入Clazz所有欄位(包含super class)
     *
     * @param clazz
     *            class
     * @param containSuperClazz
     *            是否包含繼承的Class欄位
     * @return String[]
     */
    public static String[] getFieldName(Class<?> clazz, boolean containSuperClazz) {
        Set<String> cols = new LinkedHashSet<String>();
        Class<?> searchClazz = clazz;
        while (!Object.class.equals(searchClazz) && searchClazz != null) {
            Field[] fields = searchClazz.getDeclaredFields();
            for (Field f : fields) {
                if ("serialVersionUID".equals(f.getName()))
                    continue;
                cols.add(f.getName());
            }
            searchClazz = containSuperClazz ? searchClazz.getSuperclass() : null;
        }
        return cols.toArray(new String[cols.size()]);
    }

    /**
     * 取得傳入Clazz所有欄位(包含super class)
     *
     * @param clazz
     *            class
     * @param containSuperClazz
     *            是否包含繼承的Class欄位
     * @return Field[]
     */
    public static Field[] getField(Class<?> clazz, boolean containSuperClazz) {
        Set<Field> cols = new LinkedHashSet<Field>();
        Class<?> searchClazz = clazz;
        while (!Object.class.equals(searchClazz) && searchClazz != null) {
            Field[] fields = searchClazz.getDeclaredFields();
            for (Field f : fields) {
                if ("serialVersionUID".equals(f.getName()))
                    continue;
                cols.add(f);
            }
            searchClazz = containSuperClazz ? searchClazz.getSuperclass() : null;
        }
        return cols.toArray(new Field[] {});
    }

    /**
     * @param <T>
     * @param entry
     * @param fieldId
     * @param value
     * @return
     */
    public static <T> T setField(T entry, String fieldId, Object value) {
        Field field = ReflectionUtils.findField(entry.getClass(), fieldId);
        if (field != null) {
            String setter = new StringBuffer("set").append(String.valueOf(field.getName().charAt(0)).toUpperCase()).append(field.getName().substring(1)).toString();
            Method method = ReflectionUtils.findMethod(entry.getClass(), setter, new Class[] { field.getType() });
            if (method != null) {
                try {
                    if (value == null) {
                        method.invoke(entry, new Object[] { null });
                    } else {
                        // 如果 bean field 的 type 真的是 xxxx[]，會有問題
                        value = value.getClass().isArray() ? Array.get(value, 0) : value;
                        switch (field.getType().getName()) {
                        case "java.math.BigDecimal":
                            value = CapMath.getBigDecimal(String.valueOf(value));
                            break;
                        case "java.lang.Integer":
                            value = CapMath.getBigDecimal(String.valueOf(value)).intValue();
                            break;
                        case "java.util.Date":
                            value = DateUtils.convertStringToDate((String) value);
                            break;
                        case "java.sql.Date":
                            Date date = DateUtils.convertStringToDate((String) value);
                            value = new java.sql.Date(date.getTime());
                            break;
                        case "java.sql.Timestamp":
                            if (value.getClass().toString().equals("class java.sql.Timestamp")) {
                                // todo nothing
                            } else if (value.getClass().toString().equals("class java.lang.String")) {
                                String in = (String) value;
                                if (in.indexOf(':') > 0) {
                                    // value = CapDate.convertStringToTimestamp((String) value, "yyyy/MM/dd HH:mm:ss");
                                    value = DateUtils.convertStringToTimestamp((String) value);
                                } else {
                                    value = DateUtils.convertStringToDate((String) value);
                                }
                            }
                            break;
                        default:
                            if (field.getType() != String.class && "".equals(value)) {
                                value = null;
                            }
                        }
                        // 這段判斷有點怪...
                        // if (field.getType() != String.class && "".equals(value)) {
                        // value = null;
                        // } else if (field.getType() == BigDecimal.class) {
                        // value = CapMath.getBigDecimal(String.valueOf(value));
                        // } else if (value instanceof String) {
                        // if (field.getType() == java.util.Date.class || field.getType() == java.sql.Date.class) {
                        // value = CapDate.parseDate((String) value);
                        // } else if (field.getType() == Timestamp.class) {
                        // value = CapDate.convertStringToTimestamp1((String) value);
                        // }
                        // }
//                        method.invoke(entry, ConvertUtils.convert(value, field.getType()));
                        
                        Object convertObj = null;
                        if (value != null) {
                            convertObj = ConvertUtils.convert(value, field.getType());
                        }
                        method.invoke(entry, convertObj);
                    }
                } catch (Exception e) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(e.toString());
                    } else {
                        LOGGER.warn(e.toString());
                    }
                }
            }
        }
        return entry;
    }

    /**
     * @param mapObj
     * @return
     */
    public static Map<String, String> MapObj2Str(Map<String, Object> mapObj) {
        Map<String, String> mapStr = new HashMap<String, String>();
        for (String key : mapObj.keySet()) {
            Object value = mapObj.get(key);
            if (value != null) {
                mapStr.put(key, value.toString());
            }
        }
        return mapStr;
    }

    /**
     * 將map值放入bean中
     *
     * @param <T>
     * @param map
     * @param entry
     * @param fixString
     * @param isPrefix
     * @return
     */
    public static <T extends GenericBean> T map2Bean(Map<String, Object> map, T entry, String fixString, boolean isPrefix) {
        if (map != null && !map.isEmpty()) {
            Field[] cols = getField(entry.getClass(), true);
            for (Field f : cols) {
                String colName = f.getName();
                String key = StringUtils.isEmpty(fixString) ? colName : (isPrefix) ? fixString + colName.substring(0, 1).toUpperCase() + colName.substring(1) : colName + fixString;
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    setField(entry, f.getName(), value);
                }
            }
        }
        return entry;
    }

    /**
     * 判斷傳入的角色代號是否為經辦
     *
     * @param roleCode
     * @return
     */
    public static boolean isWorkEmp(String roleCode) {
        if (roleCode.substring(1, 2).equals("1") || roleCode.startsWith("C") || roleCode.startsWith("R") || roleCode.equals("SYS")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判斷mpa內的值是否與model內相對應的值不同
     *
     * @param <T>
     * @param map
     * @param model
     * @return
     */
    public static <T extends GenericBean> boolean checkIsDiff(Map<String, Object> map, T model) {
        boolean isDiff = false; // 是否有差異
        if (model != null) {
            for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
                String key = iterator.next();
                String getter = new StringBuffer("get").append(String.valueOf(key.charAt(0)).toUpperCase()).append(key.substring(1)).toString();
                Method method = ReflectionUtils.findMethod(model.getClass(), getter);
                if (method != null) {
                    try {
                        String value = (String) map.get(key);
                        Object oriValue = method.invoke(model);
                        if (!StringUtils.isEmpty(value) && oriValue == null) {
                            isDiff = true;
                        } else if (StringUtils.isEmpty(value) && oriValue != null) {
                            isDiff = true;
                        } else if (!(value.equals(oriValue.toString()))) {
                            isDiff = true;
                        }
                        if (isDiff) {
                            break;
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOGGER.error(e.toString());
                    }
                }
            }
        }
        return isDiff;
    }

    /**
     * 複製source的值到destination(欄位相同做複製,不同不處理)
     *
     * @param <T>
     * @param source
     * @param destination
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> T copyBean(S source, T destination) {
        return copyBean(source, destination, true);
    }

    /**
     * @param <T>
     * @param source
     * @param destination
     * @param isCopyNull
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> T copyBean(S source, T destination, boolean isCopyNull) {
        if (source != null && destination != null) {
            String[] fields = CapBeanUtil.getFieldName(destination.getClass(), true);
            for (String field : fields) {
                try {
                    Object value = source.get(field); // 取值
                    if (value == null || StringUtils.isBlank(value.toString())) {
                        if (isCopyNull == false) {
                            continue;
                        }
                    }
                    destination.set(field, value); // 覆值
                } catch (CapException e) {
                    // DO NOTHING
                }
            }
        }
        return destination;
    }

    public static <S extends GenericBean, T extends GenericBean> boolean diffBean(S before, T after, String[] diffFields) {
        boolean isChange = false;
        if (diffFields == null) {
            diffFields = CapBeanUtil.getFieldName(before.getClass(), true);
        }
        for (String field : diffFields) {
            try {
                List<String> ignoreList = Arrays.asList(new String[] { "oid", "mid", "pid", "rid", "creUsrid", "creTime", "updUsrid", "updTime" });
                Object befValue = before.get(field); // 取值
                Object aftValue = after.get(field); // 取值
                if (ignoreList.contains(field) || (befValue == null && aftValue == null)) {
                    // 忽略欄位或空值沒變更
                    continue;
                } else if (befValue == null || aftValue == null) {
                    return true;
                }
                switch (befValue.getClass().getName()) {
                case "java.math.BigDecimal":
                    isChange = ((BigDecimal) befValue).compareTo(((BigDecimal) aftValue)) != 0;
                    break;
                case "java.lang.Integer":
                    isChange = ((Integer) befValue).compareTo(((Integer) aftValue)) != 0;
                    break;
                case "java.util.Date":
                case "java.sql.Date":
                    isChange = ((Date) befValue).compareTo(((Date) aftValue)) != 0;
                    break;
                case "java.sql.Timestamp":
                    isChange = ((Timestamp) befValue).compareTo(((Timestamp) aftValue)) != 0;
                    break;
                default:
                    isChange = !((String) befValue).equals((String) aftValue);
                    break;
                }
                if (isChange) {
                    return true;
                }
            } catch (CapException e) {
                // DO NOTHING
            }
        }
        return isChange;
    }

    /**
     * 比對兩個Bean欄位的值，將不同的的部分用{"欄位":{"before":before,"after":after}}的方式，回傳map
     *
     * @param before
     * @param after
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> Map<String, Map<String, Object>> diffBeanFields(S before, T after, String[] diffFields) {
        Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
        if (diffFields == null) {
            diffFields = CapBeanUtil.getFieldName(before.getClass(), true);
        }
        for (String field : diffFields) {
            try {
                String[] ignore = { "oid", "mid", "pid", "creUsrid", "creTime", "updUsrid", "updTime" };
                Object befValue = before.get(field); // 取值
                Object aftValue = after.get(field); // 取值
                int index = -1;
                for (int i = 0; i < ignore.length; i++) {
                    if (ignore[i].equals(field)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1 && befValue != null && aftValue != null && !befValue.equals(aftValue)) {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("before", befValue == null ? "" : befValue);
                    m.put("after", aftValue == null ? "" : aftValue);
                    map.put(field, m);
                }
            } catch (CapException e) {
                // DO NOTHING
            }
        }
        return map;
    }

    /**
     * 比對兩個ListBean欄位的值，只要有不同將ListBean用Json{"before":jsonStr,"after":jsonStr}的方式，回傳map
     *
     * @param before
     * @param after
     * @return
     */
    public static <S extends GenericBean, T extends GenericBean> Map<String, String> diffBeanListJson(List<S> beforeList, List<T> afterList, String[] diffFields) {
        Map<String, String> map = new HashMap<String, String>();
        Set<String> keySet = new HashSet<String>();
        if (diffFields == null) {
            diffFields = CapBeanUtil.getFieldName(beforeList.get(0).getClass(), true);
        }
        for (String field : diffFields) {
            try {
                String[] ignore = { "oid", "mid", "pid", "creUsrid", "creTime", "updUsrid", "updTime" };
                for (S b : beforeList) {
                    b.get(field); // 取值看看會不會報錯
                }
                for (T a : afterList) {
                    a.get(field); // 取值看看會不會報錯
                }
                int index = -1;
                for (int i = 0; i < ignore.length; i++) {
                    if (ignore[i].equals(field)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    keySet.add(field);
                }
            } catch (CapException e) {
                // DO NOTHING
            }
        }
        List<Map<String, Object>> beforeListMap = new ArrayList<Map<String, Object>>();
        for (S before : beforeList) {
            Map<String, Object> beforeMap = new HashMap<String, Object>();
            for (String key : keySet) {
                beforeMap.put(key, before.get(key));
            }
            beforeListMap.add(beforeMap);
        }
        String beforeJson = GsonUtil.objToJson(beforeListMap);
        List<Map<String, Object>> afterListMap = new ArrayList<Map<String, Object>>();
        for (T after : afterList) {
            Map<String, Object> afterMap = new HashMap<String, Object>();
            for (String key : keySet) {
                afterMap.put(key, after.get(key));
            }
            afterListMap.add(afterMap);
        }
        String afterJson = GsonUtil.objToJson(afterListMap);
        if (!beforeJson.equals(afterJson)) {
            map.put("before", beforeJson);
            map.put("after", afterJson);
        }
        return map;
    }

    /**
     * copyBean，且 mid,pid塞入指定的mid
     *
     * @param source
     * @param mid
     * @return
     */
    public static <T extends GenericBean> T copyBean2Mid(T source, String mid, Class<T> clazz) {
        try {
            T copy = copyBean(source, clazz.newInstance());
            copy.set("oid", null);
            copy.set("mid", mid);
            copy.set("pid", mid);
            return copy;
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return source;
    }

    /**
     * 將List內的copyBean，且 mid,pid塞入指定的mid
     *
     * @param list
     * @param mid
     * @return
     */
    public static <T extends GenericBean> List<T> copyListBean2Mid(List<T> list, String mid, Class<T> clazz) {
        List<T> newList = new ArrayList<T>();
        for (T b : list) {
            newList.add(copyBean2Mid(b, mid, clazz));
        }
        return newList;
    }

    /**
     * @param jsonObject
     * @param property
     * @return
     */
    public static String getString(JsonObject jsonObject, String property) {
        if (jsonObject == null || jsonObject.isJsonNull()) {
            return null;
        }

        JsonElement jsonElement = jsonObject.get(property);

        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        return jsonElement.getAsString();
    }

    /**
     * @param jsonObject
     * @param property
     * @return
     */
    public static JsonArray getAsJsonArray(JsonObject jsonObject, String property) {
        if (jsonObject == null || jsonObject.isJsonNull()) {
            return null;
        }

        JsonElement jsonElement = jsonObject.get(property);

        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        return jsonElement.getAsJsonArray();
    }

    public static byte[] xor(byte[] partA, byte[] partB) {
        if (partA.length != partB.length) {
            return null;
        }
        byte[] result = new byte[partA.length];
        for (int i = 0; i < partA.length; i++) {
            result[i] = (byte) (partA[i] ^ partB[i]);
        }
        return result;
    }

    /**
     * toDo
     *
     * @param str
     * @return
     */

    public static boolean checkID(String str) {

        if (str == null || "".equals(str)) {
            return false;
        }

        final char[] pidCharArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        // 原身分證英文字應轉換為10~33，這裡直接作個位數*9+10
        final int[] pidIDInt = { 1, 10, 19, 28, 37, 46, 55, 64, 39, 73, 82, 2, 11, 20, 48, 29, 38, 47, 56, 65, 74, 83, 21, 3, 12, 30 };

        // 原居留證第一碼英文字應轉換為10~33，十位數*1，個位數*9，這裡直接作[(十位數*1) mod 10] + [(個位數*9) mod 10]
        final int[] pidResidentFirstInt = { 1, 10, 9, 8, 7, 6, 5, 4, 9, 3, 2, 2, 11, 10, 8, 9, 8, 7, 6, 5, 4, 3, 11, 3, 12, 10 };

        // 原居留證第二碼英文字應轉換為10~33，並僅取個位數*8，這裡直接取[(個位數*8) mod 10]
        final int[] pidResidentSecondInt = { 0, 8, 6, 4, 2, 0, 8, 6, 2, 4, 2, 0, 8, 6, 0, 4, 2, 0, 8, 6, 4, 2, 6, 0, 8, 4 };

        str = str.toUpperCase();// 轉換大寫
        final char[] strArr = str.toCharArray();// 字串轉成char陣列
        int verifyNum = 0;

        /* 檢查身分證字號 */
        if (str.matches("[A-Z]{1}[1-2]{1}[0-9]{8}")) {
            // 第一碼
            verifyNum = verifyNum + pidIDInt[Arrays.binarySearch(pidCharArray, strArr[0])];
            // 第二~九碼
            for (int i = 1, j = 8; i < 9; i++, j--) {
                verifyNum += Character.digit(strArr[i], 10) * j;
            }
            // 檢查碼
            verifyNum = (10 - (verifyNum % 10)) % 10;

            return verifyNum == Character.digit(strArr[9], 10);
        }

        /* 檢查統一證(居留證)編號 */
        verifyNum = 0;
        if (str.matches("[A-Z]{1}[A-D]{1}[0-9]{8}")) {
            // 第一碼
            verifyNum += pidResidentFirstInt[Arrays.binarySearch(pidCharArray, strArr[0])];
            // 第二碼
            verifyNum += pidResidentSecondInt[Arrays.binarySearch(pidCharArray, strArr[1])];
            // 第三~八碼
            for (int i = 2, j = 7; i < 9; i++, j--) {
                verifyNum += Character.digit(strArr[i], 10) * j;
            }
            // 檢查碼
            verifyNum = (10 - (verifyNum % 10)) % 10;

            return verifyNum == Character.digit(strArr[9], 10);
        }
        /* 營利事業統一編號檢查程式 */
        Pattern TWBID_PATTERN = Pattern.compile("^[0-9]{8}$");

        boolean result = false;
        boolean type2 = false; // 第七個數是否為七
        if (TWBID_PATTERN.matcher(str).matches()) {
            int tmp = 0, sum = 0;
            for (int i = 0; i < 8; i++) {
                // tmp = (str.charAt(i) - '0') (weight.charAt(i) - '0');//todo
                sum += (int) (tmp / 10) + (tmp % 10); // 取出十位數和個位數相加
                if (i == 6 && str.charAt(i) == '7') {
                    type2 = true;
                }
            }
            if (type2) {
                if ((sum % 10) == 0 || ((sum + 1) % 10) == 0) { // 如果第七位數為7
                    result = true;
                }
            } else {
                if ((sum % 10) == 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    public static <T extends GenericBean> T map2BeanCust(String prefix, Map<String, Object> map, T entry, Class<?> clazz) {
        if (map != null && !map.isEmpty()) {
            Field[] cols = getField(clazz, true);
            for (Field f : cols) {
                String key = prefix + f.getName();
                if (map.containsKey(key)) {
                    Object value;
                    value = map.get(key);
                    setFieldCust(entry, f.getName(), value);
                }
            }
        }
        return entry;
    }

    /**
     * 針對空字串欄位做處理，避免發生Exception而沒有塞值
     *
     * @param entry
     * @param fieldId
     * @param value
     * @return
     */
    public static <T> T setFieldCust(T entry, String fieldId, Object value) {
        Field field = ReflectionUtils.findField(entry.getClass(), fieldId);
        if (field != null) {
            String setter = new StringBuffer("set").append(String.valueOf(field.getName().charAt(0)).toUpperCase()).append(field.getName().substring(1)).toString();
            Method method = ReflectionUtils.findMethod(entry.getClass(), setter, new Class[] { field.getType() });
            if (method != null) {
                try {
                    if (value == null) {
                        method.invoke(entry, new Object[] { null });
                    } else {
                        // 如果 bean field 的 type 真的是 xxxx[]，會有問題
                        value = value.getClass().isArray() ? Array.get(value, 0) : value;
                        switch (field.getType().getName()) {
                        case "java.math.BigDecimal":
                            if (CapString.isEmpty(String.valueOf(value))) {
                                value = null;
                            } else {
                                value = CapMath.getBigDecimal(String.valueOf(value));
                            }
                            break;
                        case "java.lang.Integer":
                            if (CapString.isEmpty(String.valueOf(value))) {
                                value = null;
                            } else {
                                value = CapMath.getBigDecimal(String.valueOf(value)).intValue();
                            }
                            break;
                        case "java.util.Date":
                        case "java.sql.Date":
                            if (CapString.isEmpty(String.valueOf(value))) {
                                value = null;
                            } else {
                                value = CapDate.parseDate((String) value);
                            }
                            break;
                        case "java.sql.Timestamp":
                            if (CapString.isEmpty(String.valueOf(value))) {
                                value = null;
                            } else {
                                String in = (String) value;
                                if (in.indexOf(':') > 0) {
                                    value = CapDate.convertStringToTimestamp((String) value, "yyyy/MM/dd HH:mm:ss");
                                } else {
                                    value = CapDate.convertStringToTimestamp((String) value, "yyyy/MM/dd");
                                }
                            }
                            break;
                        default:
                            if (field.getType() != String.class && "".equals(value)) {
                                value = null;
                            }
                        }
                        Object convertObj = null;
                        if (value != null) {
                            convertObj = ConvertUtils.convert(value, field.getType());
                        }
                        method.invoke(entry, convertObj);
                    }
                } catch (Exception e) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(e.toString());
                    } else {
                        LOGGER.warn(e.toString());
                    }
                }
            }
        }
        return entry;
    }

    /**
     * 核准號碼檢核 規則：核准方式(2)+核准號碼(5)+檢查碼(1)
     *
     * @param apprSn
     * @return
     */
    public static Boolean apprSnValidate(String apprSn) {
        Boolean check = false;
        check = (apprSn.length() == 8 && apprSn.matches("[0-9]{8}")) ? true : false;
        if (check) {
            String nums = apprSn.substring(2, 7);
            String checkNum = apprSn.substring(7, 8);
            int totalMultiplier = 0;
            String[] numsArrays = nums.split("");
            for (int i = 0; i < nums.length(); i++) {
                int multi = Integer.valueOf(numsArrays[i]) * (6 - i);
                totalMultiplier += multi;
            }
            int remainder = totalMultiplier % 11;
            remainder = 11 - remainder;
            remainder = (remainder > 9) ? (11 - remainder) : remainder;
            check = (Integer.valueOf(checkNum) == remainder) ? true : false;
        }
        return check;
    }

    /**
     * 字串前補0
     *
     * @param str
     *            <br/>
     * @param num
     *            不足幾位補零 <br/>
     * @return 補0後之字串 <br/>
     * @author Vance EX: addZeroWithValue("1",3) -> "001"
     */
    public static String addZeroWithValue(String str, int num) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> addErrorMsg(Map<String, Object> result, String item, String errMsg) {
        Map<String, String> errorMsg = (Map<String, String>) result.get("errorMsg");
        errorMsg = errorMsg == null ? new HashMap<>() : errorMsg;
        errorMsg.put(item, errMsg);
        result.put("errorMsg", errorMsg);
        return result;
    }

    public static Map<String, Object> underLine2lowerCase(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<String, Object>();
        for (String key : map.keySet()) {
            String newKey = key.toLowerCase().replace("_", "");
            newMap.put(newKey, map.get(key));
        }
        return newMap;
    }

    /**
     * 將傳入的檔案加入壓縮檔後，回傳壓縮檔的 byte[]
     *
     * @param fileBinaries
     *            Map<String, byte[]>，key 為檔名、value 為檔案內容
     * @return zip 檔的 binary
     */
    public static byte[] zipToByteArray(Map<String, byte[]> fileBinaries) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(baos));) {
            for (Entry<String, byte[]> rec : fileBinaries.entrySet()) {
                ZipEntry ze = new ZipEntry(rec.getKey());
                zipOut.putNextEntry(ze);
                zipOut.write(rec.getValue());
                zipOut.flush();
            }
        } catch (Exception e) {
            LOGGER.error("zipToByteArray fail.", e);
        }
        return baos.toByteArray();
    }

    public static Object getSearchValue(SearchSetting search, String key) {
        for (SearchModeParameter s : search.getSearchModeParameters()) {
            if (s.getKey().equals(key)) {
                return s.getValue();
            }
        }
        return null;
    }

    public static byte[] mergePdfs(List<byte[]> pdfs) {
        byte[] result = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            for (byte[] binary : pdfs) {
                InputStream in = new ByteArrayInputStream(binary);
                PdfReader reader = new PdfReader(in);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    document.setPageSize(reader.getPageSize(i));
                    document.newPage();
                    // import the page from source pdf
                    PdfImportedPage page = writer.getImportedPage(reader, i);
                    // add the page to the destination pdf
                    cb.addTemplate(page, 0, 0);
                }
            }
            os.flush();
            document.close();
            result = os.toByteArray();
        } catch (Exception e) {
            LOGGER.error("mergePdfs fail.", e);
        }
        return result;
    }

    /**
     * 民國轉西元
     * 
     * @param rgtDate
     * @return
     */
    public static Timestamp twYear2AdYear(String rgtDate) {
        if (!CapString.isEmpty(rgtDate) && rgtDate.length() <= 7) {
            rgtDate = rgtDate.length() == 7 ? rgtDate : rgtDate.length() == 5 ? rgtDate.substring(0, 5).concat("01") : null;
            if (rgtDate != null) {
                String year = rgtDate.substring(0, 3);
                String adYear = (Integer.valueOf(year) + 1911) + rgtDate.substring(3, rgtDate.length());
                return CapDate.convertStringToTimestamp(adYear, "yyyyMMdd");
            } else {
                return null;
            }
        } else if (!CapString.isEmpty(rgtDate) && rgtDate.length() >= 10 && rgtDate.length() <= 12) {
            Boolean validPattern = true;
            Integer patternRange = 0;
            String date = "";
            validPattern = rgtDate.indexOf(CapString.halfWidthToFullWidth("民國")) > -1 ? true : false;
            validPattern = rgtDate.indexOf(CapString.halfWidthToFullWidth("年")) > -1 ? true : false;
            validPattern = rgtDate.indexOf(CapString.halfWidthToFullWidth("月")) > -1 ? true : false;
            validPattern = rgtDate.indexOf(CapString.halfWidthToFullWidth("日")) > -1 ? true : false;
            if (validPattern) {
                int sy = rgtDate.indexOf(CapString.halfWidthToFullWidth("國"));
                int ey = rgtDate.indexOf(CapString.halfWidthToFullWidth("年"));
                int sm = rgtDate.indexOf(CapString.halfWidthToFullWidth("年"));
                int em = rgtDate.indexOf(CapString.halfWidthToFullWidth("月"));
                int sd = rgtDate.indexOf(CapString.halfWidthToFullWidth("月"));
                int ed = rgtDate.indexOf(CapString.halfWidthToFullWidth("日"));
                String yyy = rgtDate.substring(sy + 1, ey);
                patternRange = rgtDate.indexOf(em) - rgtDate.indexOf(sm);
                String mm = patternRange == 2 ? "0" + rgtDate.substring(sm + 1, em) : rgtDate.substring(sm, em);
                patternRange = rgtDate.indexOf(ed) - rgtDate.indexOf(sd);
                String dd = patternRange == 2 ? "0" + rgtDate.substring(sd, ed) : rgtDate.substring(sd, ed);
                date = yyy + mm + dd;
                return CapString.isNumeric(fullWidthToHalfWidth(date)) ? twYear2AdYear(fullWidthToHalfWidth(date)) : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String fullWidthToHalfWidth(String str) {
        for (char c : str.toCharArray()) {
            str = str.replaceAll("　", " ");
            if ((int) c >= 65281 && (int) c <= 65374) {
                str = str.replace(c, (char) (((int) c) - 65248));
            }
        }
        return str;
    }

    public static Map<String, String> MapSO2SS(Map<String, Object> map) {
        Map<String, String> m = new HashMap<>();
        for (String k : map.keySet()) {
            m.put(k, (String) map.get(k));
        }
        return m;
    }

    public static Map<String, Object> MapSS2SO(Map<String, String> map) {
        Map<String, Object> m = new HashMap<>();
        for (String k : map.keySet()) {
            m.put(k, map.get(k));
        }
        return m;
    }

    /*
     * 地址裁切
     */
    public static Map<String, String> parseAddress(String address) {
        String pattern = "`~!@#$^&*()=|{}':;',\\[\\].<>/?~!@#￥……&*()——|{}【】‘;:”“'。,、?";
        String patternHalf = "[" + pattern + "]";
        String patternFull = "[" + CapString.halfWidthToFullWidth(pattern) + "]";
        String token = null;
        Map<String, String> addrMap = new HashMap<>();
        if (address != null && address.length() > 0) {
            address = changeWord(address, new String[] { "巿市" });
            String cityCode = getSubAddress(address, "縣市");
            if (cityCode != null) {
                if (cityCode.length() != 3) {
                    address = cityCode + address;
                    cityCode = getSubAddress(address, "市縣");
                }
                token = cityCode;
                cityCode = changeWord(cityCode, new String[] { "台臺" });
            }
            address = cityCode == null ? address : address.replaceAll(token, "");
            addrMap.put("cityCode", cityCode);
            String townCode = address.contains("環區") ? getSubAddress(address, "鄉區鎮市")
                    : (address.contains("新市") || address.contains("環市")) ? getSubAddress(address, "區鄉鎮市") : getSubAddress(address, "區市鄉鎮");
            address = townCode == null ? address : address.replaceAll(townCode, "");
            if ("新竹市".equals(cityCode) || "嘉義市".equals(cityCode)) {
                townCode = cityCode;
            }
            addrMap.put("townCode", townCode);
            if (CapString.checkRegularMatch(address.trim(), patternHalf) || CapString.checkRegularMatch(address.trim(), patternFull) || address.indexOf("地下") > -1) {
                addrMap.put("addrRcode", "N");
                addrMap.put("addrRoad", address);
            } else {
                String addrVillage = address.indexOf("村") > address.indexOf("里") ? (address.contains("新村") ? getSubAddress(address, "里村") : getSubAddress(address, "村里"))
                        : getSubAddress(address, "里村");
                token = address.indexOf("村") > address.indexOf("里") ? (address.contains("新村") ? "里村" : "村里") : "里村";
                address = addrVillage == null ? address : address.replaceAll(addrVillage, "");
                addrVillage = addrVillage == null ? null : addrVillage.indexOf(token) > -1 ? addrVillage.replaceAll(token, "") : getSubAddress2(addrVillage, token);
                addrMap.put("addrVillage", addrVillage);
                token = "鄰";
                String addrVilzone = getSubAddress(address, token);
                address = addrVilzone == null ? address : address.replaceAll(addrVilzone, "");
                addrVilzone = addrVilzone == null ? null : addrVilzone.replaceAll(token, "");
                addrMap.put("addrVilzone", addrVilzone);
                if (address.contains("光路路")) {
                    addrMap.put("addrRoad", "光路路");
                    addrMap.put("addrRcode", "路");
                    address = address.replaceAll("光路路", "");
                } else {
                    String addrRoad = null;
                    if (address.contains("大道")) {
                        token = "大道";
                        addrRoad = getSubAddress(address, "大道").replaceAll(token, "");
                    } else if (address.contains("路")) {
                        token = "路";
                        addrRoad = getSubAddress(address, "路").replaceAll(token, "");
                    } else if (address.contains("街")) {
                        token = "街";
                        addrRoad = getSubAddress(address, "街").replaceAll(token, "");
                    } else {
                        token = "*";
                        addrRoad = address;
                        addrMap.put("addrRcode", "N");
                    }
                    address = "N".equals(addrMap.get("addrRcode")) ? address : address.replaceAll(addrRoad + token, "");
                    addrMap.put("addrRoad", addrRoad);
                    addrMap.put("addrRcode", token);
                }
                if (!"N".equals(addrMap.get("addrRcode"))) {
                    token = "段";
                    String addrSec = getSubAddress(address, token) == null ? null : getSubAddress(address, token);
                    address = addrSec == null ? address : address.replaceAll(addrSec, "");
                    addrSec = addrSec == null ? null : addrSec.replaceAll(token, "");
                    token = "巷";
                    String addrLane = getSubAddress(address, token) == null ? null : getSubAddress(address, token);
                    address = addrLane == null ? address : address.replaceAll(addrLane, "");
                    addrLane = addrLane == null ? null : addrLane.replaceAll(token, "");
                    token = "弄";
                    String addrAlley = getSubAddress(address, token) == null ? null : getSubAddress(address, token);
                    address = addrAlley == null ? address : address.replaceAll(addrAlley, "");
                    addrAlley = addrAlley == null ? null : addrAlley.replaceAll(token, "");
                    token = "號";
                    String addrNo = getSubAddress(address, token) == null ? null : getSubAddress(address, token);
                    address = addrNo == null ? address : address.replaceAll(addrNo, "");
                    addrNo = addrNo == null ? null : addrNo.replaceAll(token, "");
                    token = "之";
                    String addrNo2 = address.indexOf(token) == 0 ? parseNo2(address) : null;
                    address = addrNo2 == null ? address : address.replaceAll(token + addrNo2, "");
                    token = "樓";
                    String addrFl = getSubAddress(address, token) == null ? null : getSubAddress(address, token);
                    address = addrFl == null ? address : address.replaceAll(addrFl, "");
                    addrFl = addrFl == null ? null : addrFl.replaceAll(token, "");
                    token = "之";
                    String addrFld = address.indexOf(token) == 0 ? parseNo2(address) : null;
                    address = addrFld == null ? address : address.replaceAll(token + addrFld, "");
                    token = "室";
                    String addrRoom = getSubAddress(address, token) == null ? null : getSubAddress(address, token);
                    address = addrRoom == null ? address : address.replaceAll(addrRoom, "");
                    addrRoom = addrRoom == null ? null : addrRoom.replaceAll(token, "");
                    addrMap.put("addrSec", addrSec);
                    address = addrSec == null ? address : address.replaceAll(addrSec, "");
                    addrMap.put("addrLane", addrLane);
                    address = addrLane == null ? address : address.replaceAll(addrLane, "");
                    addrMap.put("addrAlley", addrAlley);
                    address = addrAlley == null ? address : address.replaceAll(addrAlley, "");
                    addrMap.put("addrNo", addrNo);
                    address = addrNo == null ? address : address.replaceAll(addrNo, "");
                    addrMap.put("addrNo2", addrNo2);
                    address = addrNo2 == null ? address : address.replaceAll(addrNo2, "");
                    addrMap.put("addrFl", addrFl);
                    address = addrFl == null ? address : address.replaceAll(addrFl, "");
                    addrMap.put("addrFld", addrFld);
                    address = addrFld == null ? address : address.replaceAll(addrFld, "");
                    addrMap.put("addrRoom", addrRoom);
                }
            }
        }
        return addrMap;
    }

    /**
     * 需轉換字元
     */
    private static String changeWord(String add, String[] changeWord) {
        for (int i = 0; i < changeWord.length; i++) {
            add = add.replaceAll(changeWord[i].substring(0, 1), changeWord[i].substring(1, 2));
        }
        return add;
    }

    private static String getSubAddress(String addr, String token) {
        String value = null;
        int endL = -1;
        for (int i = 0; i < token.length(); i++) {
            endL = addr.indexOf(token.charAt(i));
            if (endL > -1) {
                value = addr.substring(0, endL + 1);
                break;
            }
        }
        return value;
    }

    private static String getSubAddress2(String addr, String token) {
        String value = null;
        int endL = -1;
        for (int i = 0; i < token.length(); i++) {
            endL = addr.indexOf(token.charAt(i));
            if (endL > -1) {
                value = addr.substring(0, endL);
                break;
            }
        }
        return value;
    }

    private static String parseNo2(String address) {
        String no2 = "";
        String token = "[0-9,０-９]";
        if (!"之".equals(address.substring(0, 1))) {
            return null;
        }
        for (int i = 1; i < address.length(); i++) {
            if (address.substring(i, i + 1).matches(token)) {
                no2 = no2.concat(address.substring(i, i + 1));
            } else {
                break;
            }
        }
        return no2;
    }

    /**
     * 去除字串中所包含的空格（包括:空格(全形，半形)、製表符、換頁符等）
     *
     * @param s
     * @return
     */
    public static String removeAllBlank(String s) {
        String result = "";
        if (null != s && !"".equals(s)) {
            result = s.replaceAll("[　*| *| *|//s*]*", "");
        }
        return result;
    }

    /**
     * 去除字串中頭部和尾部所包含的空格（包括:空格(全形，半形)、製表符、換頁符等）
     *
     * @param s
     * @return
     */
    public static String trim(String s) {
        String result = "";
        if (null != s && !"".equals(s)) {
            result = s.replaceAll("^[　*| *| *|//s*]*", "").replaceAll("[　*| *| *|//s*]*$", "");
        }
        return result;
    }

    /**
     * 取得上傳路徑
     *
     * @param folderName
     *            mid或handler名稱
     * @return
     */
    public static String getUploadPath(String folderName) {
        //Path Manipulation 弱掃問題,所以改成寫死路徑,無法取得config.properties裡面的變數設定
        //String path = PropUtil.getProperty("upload.path") + folderName + "/";
        String path = "/ecolnfs/" + folderName + "/";
        File pathFile = new File(EloanUtils.cleanPath(path));
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        return path;
    }

    /**
     * @param listMap
     * @param valMap
     * @return
     */
    public static List<Map<String, Object>> addListMapColumnValByMap(List<Map<String, Object>> listMap, Map<String, Object> valMap) {
        for (Map<String, Object> map : listMap) {
            for (String key : valMap.keySet()) {
                map.put(key, valMap.get(key));
            }
        }
        return listMap;
    }

    /**
     * 將BigDecimal轉為補0字串
     *
     * @param b
     *            來源數字
     * @param precision
     *            有效位數
     * @param scale
     *            精度
     * @param isSign
     *            是否需要正負符號
     * @param isDecimalPoint
     *            是否需要小數點
     * @return
     */
    public static String padZeroToString(BigDecimal b, int precision, int scale, boolean isSign, boolean isDecimalPoint) {
        String result = null;
        if (b != null) {
            StringBuilder sb = new StringBuilder();
            if (precision < 1) {
                return "";
            }
            for (int x = 0; x < precision; x++) {
                sb.append('0');
            }
            if (scale > 0) {
                sb.insert(precision - scale, '.');
            }
            DecimalFormat df = new DecimalFormat(sb.toString());
            if (isSign) {
                if (b.compareTo(BigDecimal.ZERO) >= 0) {
                    df.setPositivePrefix("+");
                } else {
                    df.setNegativePrefix("-");
                }
            }
            result = df.format(b);
            if (!isDecimalPoint) {
                result.replace(".", "");
            }
        }
        return result;
    }

    /**
     * 清空超過設定長度的屬性值
     *
     * @param <T>
     * @param entry
     * @return
     */
    public static <T extends GenericBean> T emptyPropertyValue(T entry) {
        Field[] cols = CapBeanUtil.getField(entry.getClass(), true);
        Table table = entry.getClass().getAnnotation(Table.class);
        for (Field f : cols) {
            Column column = f.getAnnotation(Column.class);
            Object value = entry.get(f.getName());
            if (value != null && column != null && value.toString().getBytes().length > column.length()) {
                LOGGER.info("欄位輸入的值太長：" + table.name() + "." + column.name() + "， value：" + value, entry.getClass());
                setField(entry, f.getName(), null);
            }
        }
        return entry;
    }

    public final static String concat(Object... params) {
        StringBuffer strBuf = new StringBuffer();
        for (Object o : params) {
            if (o instanceof byte[]) {
                strBuf.append(new String((byte[]) o, StandardCharsets.UTF_8));
            } else {
                strBuf.append(String.valueOf(o));
            }
        }
        return strBuf.toString();
    }

    /**
     * 格式化BigDecimal，預設格式: #,##0，ex:1,234
     *
     * @param b
     * @param format
     * @return
     */
    public static String formatBigDecimal(BigDecimal b, String format) {
        String result = "";
        // 設定格式
        if (StringUtils.isBlank(format)) {
            if (new BigDecimal(b.intValue()).compareTo(b)==0){
                //整數
                format = "#,##0";                
            }else {
                //小數
                format = "#.####";
            }
        }
        DecimalFormat df = new DecimalFormat(format);
        if (null != b) {
            result = df.format(b);
        } else {
            // 傳入數字為null，給0
            result = df.format(BigDecimal.ZERO);
        }
        return result;
    }

    public final static String concatWithTrim(Object... params) {
        StringBuffer strBuf = new StringBuffer();
        for (Object o : params) {
            if (null == o) {
                continue;
            }

            if (o instanceof byte[]) {
                strBuf.append(new String((byte[]) o, StandardCharsets.UTF_8));
            } else {
                strBuf.append(StringUtils.trimToEmpty(String.valueOf(o)));
            }
        }
        return strBuf.toString();
    }

    public static String concat(String linkStr, String... args) {
        if (ArrayUtils.isEmpty(args)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : args) {
            if (StringUtils.isBlank(str)) {
                continue;
            }
            sb.append(StringUtils.trimToEmpty(linkStr));
            sb.append(StringUtils.trimToEmpty(str));
        }
        if (StringUtils.isBlank(sb.toString())) {
            return "";
        } else {
            return sb.toString().substring(1);
        }
    }

    public static String convertFlag(String flg) {
        if (StringUtils.isBlank(flg)) {
            return "";
        }
        String yn = flg;
        if (StringUtils.equalsIgnoreCase("1", flg)) {
            flg = "Y";
        }

        if (StringUtils.equalsIgnoreCase("0", flg)) {
            flg = "N";
        }

        return flg;
    }

    public static Map<String, String> getCustInfoColMap() {
        Map<String, String> colMap = new HashMap<String, String>();
        colMap.put("custId", "客戶ID");
        colMap.put("custName", "客戶名稱");
        colMap.put("custStatus", "債務人狀況");
        colMap.put("specialType", "債務人類型");
        colMap.put("specialMapName", "對應人");
        colMap.put("birthday", "生日");
        colMap.put("sex", "性別");
        colMap.put("cellPhone", "行動電話");
        colMap.put("cellPhone2", "行動電話2");
        colMap.put("email", "電子郵件信箱");
        colMap.put("email2", "電子郵件信箱2");
        colMap.put("birthPlace", "出生地/設立地");
        colMap.put("industryType", "行業別");
        colMap.put("hostAddrZip", "戶籍地址郵遞區號");
        colMap.put("hostAddr", "戶籍地址");
        colMap.put("homeAddrZip", "通訊地址郵遞區號");
        colMap.put("homeAddr", "通訊地址");
        colMap.put("homePhone", "住家電話");
        colMap.put("compAddrZip", "公司地址郵遞區號");
        colMap.put("compAddr", "公司地址");
        colMap.put("compPhone", "公司電話");
        colMap.put("compPhoneExt", "公司分機號碼");
        colMap.put("compName", "任職公司名稱");
        colMap.put("ownerId", "負責人統編");
        colMap.put("ownerName", "負責人姓名");
        colMap.put("occupType", "職業別");
        colMap.put("jobTitle", "職稱");
        colMap.put("jobStatus", "工作狀態");
        colMap.put("annualIncome", "年收入(年營業額)");
        colMap.put("contactAddrZip", "聯絡地址郵遞區號1");
        colMap.put("contactAddr", "聯絡地址1");
        colMap.put("contactPhone", "聯絡電話1");
        colMap.put("contactAddrZip2", "聯絡地址郵遞區號2");
        colMap.put("contactAddr2", "聯絡地址2");
        colMap.put("contactPhone2", "聯絡電話2");
        colMap.put("remark", "備註");
        return colMap;
    }

    public static Map<String, String> getGuarInfoColMap() {
        Map<String, String> guarMap = new HashMap<String, String>();
        guarMap.put("Host", "是否主機下傳");
        guarMap.put("guarId", "客戶ID");
        guarMap.put("guarName", "客戶名稱");
        guarMap.put("sex", "性別");
        guarMap.put("birthday", "生日");
        guarMap.put("ownerId", "負責人統編");
        guarMap.put("ownerName", "負責人姓名");
        guarMap.put("industryType", "行業別");
        guarMap.put("birthPlace", "出生地/設立地");
        guarMap.put("guarType", "保證人性質");
        guarMap.put("guarRelation", "與債務人關係");
        guarMap.put("guarResp", "保證責任比例");
        guarMap.put("guarRespPercent", "保證百分比");
        guarMap.put("cellPhone", "行動電話");
        guarMap.put("cellPhone2", "行動電話2");
        guarMap.put("email", "電子郵件信箱");
        guarMap.put("email2", "電子郵件信箱2");
        guarMap.put("collPercent", "擔保品持有百分比");
        guarMap.put("hostAddrZip", "戶籍地址郵遞區號");
        guarMap.put("hostAddr", "戶籍地址");
        guarMap.put("homeAddrZip", "通訊地址郵遞區號");
        guarMap.put("homeAddr", "通訊地址");
        guarMap.put("homePhone", "住家電話");
        guarMap.put("compAddrZip", "公司地址郵遞區號");
        guarMap.put("compAddr", "公司地址");
        guarMap.put("compPhone", "公司電話");
        guarMap.put("compPhoneExt", "公司分機號碼");
        guarMap.put("compName", "任職公司名稱");
        guarMap.put("occupType", "職業別");
        guarMap.put("jobTitle", "職稱");
        guarMap.put("jobStatus", "工作狀態");
        guarMap.put("annualIncome", "年收入(年營業額)");
        guarMap.put("guarStatus", "債務人狀況");
        guarMap.put("specialType", "債務人類型");
        guarMap.put("specialMapName", "對應人");
        guarMap.put("contactAddrZip", "聯絡地址郵遞區號1");
        guarMap.put("contactAddr", "聯絡地址1");
        guarMap.put("contactPhone", "聯絡電話1");
        guarMap.put("contactAddrZip2", "聯絡地址郵遞區號2");
        guarMap.put("contactAddr2", "聯絡地址2");
        guarMap.put("contactPhone2", "聯絡電話2");
        guarMap.put("remark", "備註");
        guarMap.put("isNoGuar", "是否解除保證責任");
        guarMap.put("acctList", "保證帳號明細");
        return guarMap;
    }

    public static Map<String, String> getRelInfoColMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Host", "是否主機下傳");
        map.put("isHost", "是否主機下傳");
        map.put("isBill", "是否為票據關係人");
        map.put("relId", "客戶ID");
        map.put("relName", "客戶名稱");
        map.put("sex", "性別");
        map.put("birthday", "生日");
        map.put("ownerId", "負責人統編");
        map.put("ownerName", "負責人姓名");
        map.put("industryType", "行業別");
        map.put("birthPlace", "出生地/設立地");
        map.put("cellPhone", "行動電話");
        map.put("cellPhone2", "行動電話2");
        map.put("email", "電子郵件信箱");
        map.put("email2", "電子郵件信箱2");
        map.put("relType", "關係人性質");
        map.put("relRelation", "與債務人關係");
        map.put("relRemark", "關係事項說明");
        map.put("hostAddrZip", "戶籍地址郵遞區號");
        map.put("hostAddr", "戶籍地址");
        map.put("homeAddrZip", "通訊地址郵遞區號");
        map.put("homeAddr", "通訊地址");
        map.put("homePhone", "住家電話");
        map.put("compAddrZip", "公司地址郵遞區號");
        map.put("compAddr", "公司地址");
        map.put("compPhone", "公司電話");
        map.put("compPhoneExt", "公司分機號碼");
        map.put("compName", "任職公司名稱");
        map.put("occupType", "職業別");
        map.put("jobTitle", "職稱");
        map.put("relStatus", "債務人狀況");
        map.put("contactAddrZip", "聯絡地址郵遞區號1");
        map.put("contactAddr", "聯絡地址1");
        map.put("contactPhone", "聯絡電話1");
        map.put("contactAddrZip2", "聯絡地址郵遞區號2");
        map.put("contactAddr2", "聯絡地址2");
        map.put("contactPhone2", "聯絡電話2");
        map.put("remark", "備註");
        return map;
    }

    public static Map<String, String> getLawRelDtlColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("relKind", "相對人關係");
        map.put("relId", "相對人ID");
        map.put("relName", "相對人姓名");
        map.put("addr", "戶籍地或設定地址");
        map.put("causeStatus", "確定情形");
        map.put("objectDt", "聲明異議日期");
        return map;
    }

    public static Map<String, String> getLawAmtDtlColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("currency", "幣別");
        map.put("lawAmt", "訴訟標的金額原幣");
        map.put("exRate", "匯率");
        map.put("lawAmtNTD", "訴訟標的金額台幣");
        map.put("exRateDt", "匯率日期");
        return map;
    }

    public static Map<String, String> getLawCertDtlColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("srcOid", "來源資料oid");
        map.put("relKind", "相對人關係");
        map.put("relId", "相對人ID");
        map.put("relName", "相對人姓名");
        map.put("addr", "戶籍地或設定地址");
        return map;
    }

    public static Map<String, String> getLawRecColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("procSer", "流程種類代號");
        map.put("stateSer", "法務階段");
        map.put("custId", "借戶CIKEY");
        map.put("custName", "借戶名稱");
        map.put("userId", "催收人員");
        map.put("userName", "催收人員名稱");
        map.put("updDate", "最後處理日期");
        map.put("activeFlg", "實體啟用註記");
        map.put("returnStateSer", "記錄流程分岔點");
        map.put("chNameList", "相對人姓名");
        map.put("courtId", "法院代碼");
        map.put("lawSection", "股別");
        map.put("lawYear", "公文年度");
        map.put("lawWord", "公文字號");
        map.put("lawNo", "公文文號");
        map.put("lawAmt", "標的金額");
        map.put("currLawAmt", "標的金額(外幣)");
        map.put("cancelReason", "取消原因");
        map.put("approver", "核准人員號碼");
        map.put("approveTime", "核准日期");
        return map;
    }

    public static Map<String, String> getLawCauseColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("plaintDt", "撰狀日期");
        map.put("renderDt", "遞狀日期");
        map.put("docSendDt", "發文日期");
        map.put("docRcvdDt", "收文日期");
        map.put("confirmPlaintDt", "聲請確證撰狀日");
        map.put("confirmRenderDt", "聲請確證遞狀日");
        map.put("recallPlaintDt", "撤回撰狀日");
        map.put("recallRenderDt", "撤回遞狀日");
        map.put("corrPlaintDt", "更正裁定撰狀日");
        map.put("corrRenderDt", "更正裁定遞狀日");
        map.put("bullPlaintDt", "公示送達撰狀日");
        map.put("bullRenderDt", "公示送達遞狀日");
        map.put("paperDt", "登報日期");
        map.put("paperRemark", "登報備註");
        map.put("againstDt", "抗告日期");
        map.put("againstRemark", "抗告備註");
        map.put("recallPIdList", "撤回身分證號");
        map.put("resultStatus", "支命登入完成狀態");
        map.put("remark", "大表備註");
        return map;
    }

    public static Map<String, String> getLawCorrectColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("noticeCourtId", "通知法院代碼");
        map.put("courtId", "管轄法院代碼");
        map.put("docRcvdDt", "收文日期");
        map.put("correctItem", "補正事項");
        map.put("correctRemart", "陳報內容");
        map.put("plaintDt", "撰狀日");
        map.put("renderDt", "遞狀日");
        map.put("remark", "大表備註");
        return map;
    }

    public static Map<String, String> getLawDepositColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("orgOid", "原序號");
        map.put("newOid", "新序號");
        map.put("retriveCourtId", "聲請取回法院代碼");
        map.put("retrivePlaintDt", "聲請取回撰狀日");
        map.put("retriveRenderDt", "聲請取回遞狀日");
        map.put("chngCourtId", "聲請變換法院代碼");
        map.put("chngPlaintDt", "聲請變換撰狀日");
        map.put("chngRenderDt", "聲請變換遞狀日");
        map.put("chngJugDt", "裁定日期");
        map.put("chngJugCourt", "裁定法院");
        map.put("chngJugSection", "裁定股別");
        map.put("chgnJugYear", "法院年度");
        map.put("chgnJugWord", "法院字號");
        map.put("chgnJugNo", "法院文號");
        map.put("retriveDocDt", "收取回提存申請書日期");
        map.put("retriveSection", "法院股別");
        map.put("retriveYear", "法院年度");
        map.put("retriveWord", "法院字號");
        map.put("retriveNo", "法院文號");
        map.put("returnDt", "取回日期");
        map.put("addDocRemark", "補件備註");
        map.put("attachment", "更送相關文件備註");
        map.put("dpstAmt", "新提存金額");
        map.put("dpstType", "新擔保金種類");
        map.put("defBondOid", "新債卷名稱oid");
        map.put("dpstDate", "新提存日期");
        map.put("expireDt", "新到期日");
        map.put("bondNo", "新公債代號");
        map.put("remark", "大表備註");
        return map;
    }

    public static Map<String, String> getLawCertColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("activeFlg", "最新註記");
        map.put("subMainId", "subMainId");
        map.put("refSubMainId", "refSubMainId");
        map.put("procSer", "程序類型");
        map.put("orgCertTypeSer", "原始執行名義代碼");
        map.put("certTypeMSer", "執行名義大類");
        map.put("certTypeSer", "執行名義種類");
        map.put("certName", "執行名義名稱");
        map.put("borrowDate", "借出日期");
        map.put("returnDate", "歸還日期");
        map.put("docCourtId", "發給法院");
        map.put("docDate", "發文日期");
        map.put("certDate", "時效起算日");
        map.put("effectPeriod", "執行名義時效");
        map.put("dueDate", "時效完成日");
        map.put("chNameList", "相對人姓名");
        map.put("lawCertSeqNo", "執行名義序號");
        map.put("certAcct", "帳號");
        map.put("interruptDate", "時效中斷日");
        map.put("interruptReason", "時效中斷事由");
        map.put("lawAmt", "債權本金");
        map.put("lawRate", "債證利率");
        map.put("currLawAmt", ")");
        map.put("intStartDate", "利息起算日");
        map.put("penaltyDate", "違約金起算日");
        map.put("penaltyCalType", "違約金計算方式");
        map.put("lawActFee", "執行費用");
        map.put("lawProcFee", "程序費用");
        map.put("lawActUser", "執行費用負擔人");
        map.put("lawProcUser", "程序費用負擔人");
        map.put("keepUser", "保管人員");
        map.put("certStatus", "執行名義狀態");
        map.put("voidReason", "作廢理由");
        map.put("remark", "備註");
        map.put("oldLawCertPid", "原執行名義序號");
        map.put("hisLawCertPid", "歷史執行名義序號");
        map.put("courtId", "法院代碼");
        map.put("docSection", "股別");
        map.put("docYear", "公文年度");
        map.put("docWord", "公文字號");
        map.put("docNo", "公文文號");
        map.put("borrowUserName", "借出人員名稱");
        map.put("borrowUserId", "借出人員代碼");
        map.put("returnUserName", "歸還人員名稱");
        map.put("returnUserId", "歸還人員代碼");
        return map;
    }

    public static Map<String, String> getLawAccuseColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("lawCertOid", "確定證明lawcertoid");
        map.put("lawCertOidH", "和解筆錄lawcertoid");
        map.put("lawCertOidT", "調解筆錄lawcertoid");
        map.put("trialLevel", "審級");
        map.put("previousTrialNo", "前審案號");
        map.put("appellant", "上訴人");
        map.put("docSendDt", "發文日期");
        map.put("docRcvdDt", "收文日期");
        map.put("courtDt1", "第一次開庭日");
        map.put("courtDt2", "第二次開庭日");
        map.put("courtDt3", "第三次開庭日");
        map.put("courtDt4", "第四次開庭日");
        map.put("courtDt5", "第五次開庭日");
        map.put("note1", "第一次開庭注意事項");
        map.put("note2", "第二次開庭注意事項");
        map.put("note3", "第三次開庭注意事項");
        map.put("note4", "第四次開庭注意事項");
        map.put("note5", "第五次開庭注意事項");
        map.put("courtDt", "第一次開庭日");
        map.put("note", "第一次開庭注意事項");
        map.put("plaintDt", "起訴撰狀日");
        map.put("renderDt", "起訴遞狀日");
        map.put("confirmPlaintDt", "聲請確證撰狀日");
        map.put("confirmRenderDt", "聲請確證遞狀日");
        map.put("recallPlaintDt", "撤回撰狀日");
        map.put("recallRenderDt", "撤回遞狀日");
        map.put("corrPlaintDt", "更正裁定撰狀日");
        map.put("corrRenderDt", "更正裁定遞狀日");
        map.put("feePlaintDt", "裁定訴訟費用撰狀日");
        map.put("feeRenderDt", "裁定訴訟費用遞狀日");
        map.put("bullPlaintDt", "公示送達日期");
        map.put("bullRenderDt", "公示送達備註");
        map.put("paperDt", "登報日期");
        map.put("paperRemark", "登報備註");
        map.put("recallPIdList", "撤回身分證號");
        map.put("resultStatus", "登錄完成狀態");
        map.put("notApReason", "不在上訴原因");
        map.put("remark", "大表備註");
        map.put("lawFirm", "律師事務所");
        map.put("lawyerName", "律師名稱");
        map.put("lawyerFee", "律師委託費");
        return map;
    }

    public static Map<String, String> getLawDistrainColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("bondOid", "提存物Oid");
        map.put("distrainType", "案別");
        map.put("dpstYN", "是否辦理提存");
        map.put("plaintDt", "聲請裁定撰狀日");
        map.put("renderDt", "聲請裁定遞狀日");
        map.put("judgeSection", "裁全股別");
        map.put("judgeYear", "裁全公文年度");
        map.put("judgeWord", "裁全公文字號");
        map.put("judgeNo", "裁全公文文號");
        map.put("judgeDocSendDt", "發文日期");
        map.put("judgeDocRcvdDt", "收文日期");
        map.put("enforcePlaintDt", "聲請執行撰狀日");
        map.put("enforceRenderDt", "聲請執行遞狀日");
        map.put("lawSection", "執全股別");
        map.put("lawYear", "執全公文年度");
        map.put("lawWord", "執全公文字號");
        map.put("lawNo", "執全公文文號");
        map.put("docSendDt", "發文日期");
        map.put("docRcvdDt", "收文日期");
        map.put("appPlaintDt", "追加撰狀日");
        map.put("appRenderDt", "追加遞狀日");
        map.put("recallPlaintDt", "部分撤回撰狀日");
        map.put("recallRenderDt", "部分撤回遞狀日");
        map.put("recallAllPlaintDt", "全案撤回撰狀日");
        map.put("recallAllRenderDt", "全案撤回遞狀日");
        map.put("appPIdList", "追加身分證號");
        map.put("recallPIdList", "撤回身分證號");
        map.put("recallRemark", "部分撤回原因");
        map.put("recallAllRemark", "全案撤回原因");
        map.put("remark", "大表備註");
        return map;
    }

    public static Map<String, String> getLawDistrainPropColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("propSource", "來源");
        map.put("propOid", "財產序號");
        map.put("propType", "財產種類");
        map.put("propName", "財產種類名稱");
        map.put("ownerNameList", "所有權人名稱");
        map.put("propDesc", "財產描述");
        map.put("stateSer", "子流程狀態");
        map.put("recallPlaintDt", "撰狀聲請撤回日");
        map.put("recallRenderDt", "遞狀聲請撤回日");
        map.put("conductDt", "導往日");
        map.put("mergeCourtId", "併案法院代碼");
        map.put("mergeSection", "股別");
        map.put("mergeYear", "年度");
        map.put("mergeWord", "字號");
        map.put("mergeNo", "文號");
        map.put("mergeDocRcvdDt", "收文日期");
        map.put("entrustCourtId", "囑託法院代碼");
        map.put("entrustSection", "股別");
        map.put("entrustYear", "執全公文年度");
        map.put("entrustWord", "執全公文字號");
        map.put("entrustNo", "執全公文文號");
        map.put("entrustDocRcvdDt", "收文日期");
        map.put("distResult", "查封結果");
        map.put("remark", "大表備註");
        map.put("returnStateSer", "紀錄流程分岔點");
        map.put("updater", "目前催收人員");
        map.put("updateTime", "最後處理日期");
        map.put("traceDate", "追蹤日期");
        map.put("renderFlag", "待遞狀");
        return map;
    }

    public static Map<String, String> getLawEnforceColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("enforceType", "強執種類");
        map.put("lawCertOid", "原執行名義序號");
        map.put("plaintDt", "撰狀日");
        map.put("renderDt", "遞狀日");
        map.put("docSendDt", "發文日期");
        map.put("docRcvdDt", "收文日期");
        map.put("certDocSendDt", "發文日期");
        map.put("certDocRcvdDt", "收文日期");
        map.put("certDocRemark", "備註");
        map.put("recallPlaintDt", "部分撤回撰狀日");
        map.put("recallRenderDt", "部分撤回遞狀日");
        map.put("recallAllPlaintDt", "全案撤回撰狀日");
        map.put("recallAllRenderDt", "全案撤回遞狀日");
        map.put("appPlaintDt", "追加撰狀日");
        map.put("appRenderDt", "追加遞狀日");
        map.put("borrowPlaintDt", "借回債權憑證撰狀日");
        map.put("borrowRenderDt", "借回債權憑證遞狀日");
        map.put("mergePlaintDt", "併案撰狀日");
        map.put("mergeRenderDt", "併案遞狀日");
        map.put("suspendPlaintDt", "暫緩執行撰狀日");
        map.put("suspendRenderDt", "暫緩執行遞狀日");
        map.put("suspendDt", "具名筆錄暫緩執行日");
        map.put("suspendRemark", "具名筆錄暫緩執行備註");
        map.put("resumeDt", "聲請續行強執日");
        map.put("resumeRemark", "聲請續行備註");
        map.put("mergeLawCertUid", "併案執行名義");
        map.put("mergePIdList", "併案身分證號");
        map.put("appPIdList", "追加身分證號");
        map.put("recallPIdList", "撤回身分證號");
        map.put("recallRemark", "部分撤回原因");
        map.put("recallAllRemark", "全案撤回原因");
        map.put("remark", "大表備註");
        return map;
    }

    public static Map<String, String> getLawEnforcePropColMap() {
        Map<String, String> map = new HashMap<>();
        map.put("propSource", "來源");
        map.put("propOid", "財產序號");
        map.put("propType", "財產種類");
        map.put("propName", "財產種類名稱");
        map.put("ownerNameList", "所有權人名稱");
        map.put("propDesc", "財產描述");
        map.put("stateSer", "子流程狀態");
        map.put("bidName", "投標名稱");
        map.put("appraiseDt", "收鑑價通知日期");
        map.put("appraiseRemark", "收鑑價通知備註");
        map.put("appraisePlaintDt", "免繳鑑價費撰狀日");
        map.put("appraiseRenderDt", "免繳鑑價費遞狀日");
        map.put("conductDt", "導往日");
        map.put("mergeCourtId", "併案法院代碼");
        map.put("mergeSection", "股別");
        map.put("mergeYear", "年度");
        map.put("mergeWord", "字號");
        map.put("mergeNo", "文號");
        map.put("mergeDocRcvdDt", "收文日期");
        map.put("entrustCourtId", "囑託法院代碼");
        map.put("entrustSection", "股別");
        map.put("entrustYear", "囑託公文年度");
        map.put("entrustWord", "囑託公文字號");
        map.put("entrustNo", "囑託公文文號");
        map.put("entrustDocRcvdDt", "收文日期");
        map.put("saleDt1", "一拍日期");
        map.put("saleDt2", "二拍日期");
        map.put("saleDt3", "三拍日期");
        map.put("saleDt4", "四拍日期");
        map.put("saleDateAnce1", "公告應買日期起");
        map.put("saleDateAnce2", "公告應買日期迄");
        map.put("saleDateFinal", "拍定日");
        map.put("saleDate", "公告應買日期起");
        map.put("floorPrice1", "一拍底價");
        map.put("floorPrice2", "二拍底價");
        map.put("floorPrice3", "三拍底價");
        map.put("floorPrice4", "四拍底價");
        map.put("currFloorPrice", "最新底價外幣");
        map.put("currFloorPrice1", "一拍底價外幣");
        map.put("currFloorPrice2", "二拍底價外幣");
        map.put("currFloorPrice3", "三拍底價外幣");
        map.put("currFloorPrice4", "四拍底價外幣");
        map.put("floorPriceAnnounce", "公告金額");
        map.put("currFPAnnounce", "公告金額外幣");
        map.put("floorPriceFinal", "拍定金額");
        map.put("currFPFinal", "拍定金額外幣");
        map.put("floorPrice", "一拍底價");
        map.put("handOver1", "一拍是否點交");
        map.put("handOver2", "二拍是否點交");
        map.put("handOver3", "三拍是否點交");
        map.put("handOver4", "四拍是否點交");
        map.put("handOverAnnounce", "公告是否點交");
        map.put("handOverFinal", "拍定是否點交");
        map.put("handOver", "一拍是否點交");
        map.put("remark1", "一拍備註");
        map.put("remark2", "二拍備註");
        map.put("remark3", "三拍備註");
        map.put("remark4", "四拍備註");
        map.put("expectRefund", "預估受償金額");
        map.put("currExpectRefund", "預估受償金額外幣");
        map.put("expectReturnDt", "預估領回時間");
        map.put("quoteDt", "鑑價完成日");
        map.put("quoteAmt", "鑑價金額");
        map.put("currQuoteAmt", "鑑價金額外幣");
        map.put("quoteOrg", "鑑價機構");
        map.put("quoteRemark", "鑑價備註");
        map.put("measureDt", "測量日");
        map.put("measureRemark", "測量備註");
        map.put("reducePlaintDt", "減價再拍賣撰狀日");
        map.put("reduceRenderDt", "減價再拍賣遞狀日");
        map.put("quoteObjectPlaintDt", "詢價異議撰狀日");
        map.put("quoteObjectRenderDt", "詢價異議遞狀日");
        map.put("auctionPlaintDt", "聲請拍賣撰狀日");
        map.put("auctionRenderDt", "聲請拍賣遞狀日");
        map.put("allotObjectPlaintDt", "分配表異議撰狀日");
        map.put("allotObjectRenderDt", "分配表異議遞狀日");
        map.put("rptPlaintDt", "陳報債權計算撰狀日");
        map.put("rptRenderDt", "陳報債權計算遞狀日");
        map.put("rptAmt", "陳報債權計算申報金額");
        map.put("currRptAmt", "陳報債權計算申報金額外幣");
        map.put("intEndDt", "陳報債權利息計算止日");
        map.put("allotDocRcvdDt", "分配款收文日期");
        map.put("allotDt", "分配日期");
        map.put("allotLawFee", "執行費");
        map.put("allotLawAmt1", "優先債權");
        map.put("currAllotLawAmt1", "優先債權外幣");
        map.put("allotLawAmt2", "普通債權");
        map.put("currAllotLawAmt2", "普通債權外幣");
        map.put("allotLawRemark", "備註");
        map.put("noticeCollectAmt", "領回金額");
        map.put("currNoticeCollectAmt", "領回金額外幣");
        map.put("noticeCollectDt", "領款日期");
        map.put("noticeCollectRemark", "備註");
        map.put("collectAmt", "分配金額");
        map.put("currCollectAmt", "分配金額外幣");
        map.put("collectDt", "領款日期");
        map.put("collectExecDt", "執行日期");
        map.put("collectRemark", "備註");
        map.put("transDt", "承受日期");
        map.put("seiClsReason", "扣薪結案原因");
        map.put("remark", "一拍備註");
        map.put("returnStateSer", "紀錄流程分岔點");
        map.put("updater", "目前催收人員");
        map.put("updateTime", "最後處理日期");
        map.put("traceDate", "追蹤日期");
        map.put("renderFlag", "待遞狀");
        map.put("subType", "子流程分類");
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T extends GenericBean> T getBeanByJson(String json, String key, Class<T> clazz) {
        T bean = null;
        try {
            Map<String, Object> map = GsonUtil.jsonToMap(json);
            if (MapUtils.isNotEmpty(map) && MapUtils.isNotEmpty((Map<String, Object>) map.get(key))) {
                bean = map2Bean((Map<String, Object>) map.get(key), clazz.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            //e.printStackTrace();
            LOGGER.error(e.toString());
        }

        return bean;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends GenericBean> List<T> getListByJson(String json, String key, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        try {
            Map<String, Object> map = GsonUtil.jsonToMap(json);
            if (MapUtils.isNotEmpty(map)) {
                List dataList = (List) map.get(key);
                if (CollectionUtils.isNotEmpty(dataList)) {
                    for (int i = 0; i < dataList.size(); i++) {
                        T bean = map2Bean((Map<String, Object>) dataList.get(i), clazz.newInstance());
                        list.add(bean);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            //e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return list;
    }

    public static boolean isJson(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            try {
                new JSONArray(json);
            } catch (JSONException e1) {
                return false;
            }
        }
        return true;
    }

    private static final Set<String> IGNORE_COLUMNS = Sets.newHashSet("oid", "pid", "mainId", "subMainId", "isHost", "creUserId", "creUserName", "creDate", "updUserId", "updUserName", "updDate");

    public static boolean objIsBlank(Object obj) {
        if (obj instanceof String) {
            return StringUtils.isBlank(String.valueOf(obj));
        } else {
            if (null == obj) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static <T extends DataObject> String getDiffString(T src, T dest, Map<String, String> propNameMap) {

        if (null == src || null == dest) {
            return null;
        }
        StringBuilder msg = new StringBuilder();

        Class<? extends DataObject> dataClass = src.getClass();
        String[] cols = CapBeanUtil.getFieldName(dataClass, false);
        String[] ignoreCols = new String[] { "oid", "pid", "mainId", "subMainId", "isHost", "creUserId", "creUserName", "creDate", "updUserId", "updUserName", "updDate" };
        for (String col : cols) {

            // 不需比較欄位
            if (ArrayUtils.contains(ignoreCols, col)) {
                continue;
            }

            PropertyDescriptor pd;
            try {
                pd = new PropertyDescriptor(col, dataClass);
                Object oldVal = pd.getReadMethod().invoke(src);
                Object newVal = pd.getReadMethod().invoke(dest);

                if (objIsBlank(newVal) && !objIsBlank(oldVal)) {
                    msg.append(";");
                    msg.append(StringUtils.trimToEmpty(propNameMap.get(col)));
                    msg.append("由[");
                    if (oldVal instanceof String) {
                        msg.append((String) oldVal);
                    } else if (oldVal instanceof java.util.Date) {
                        msg.append(CapDate.formatDate((java.util.Date) oldVal, CapDate.DEFAULT_DATE_FORMAT));
                    }
                    msg.append("]");
                    msg.append("變為[]");
                }

                if (!objIsBlank(newVal) && objIsBlank(oldVal)) {
                    msg.append(";");
                    msg.append(StringUtils.trimToEmpty(propNameMap.get(col)));
                    msg.append("由[]");
                    msg.append("變為[");
                    if (newVal instanceof String) {
                        msg.append((String) newVal);
                    } else if (newVal instanceof java.util.Date) {
                        msg.append(CapDate.formatDate((java.util.Date) newVal, "YYYMMDD"));
                    }
                }

                if (!objIsBlank(newVal) && !objIsBlank(oldVal)) {
                    if (newVal instanceof String) {
                        // 字串
                        if (!StringUtils.equals(StringUtils.trimToEmpty((String) newVal), StringUtils.trimToEmpty((String) oldVal))) {
                            msg.append(";");
                            msg.append(StringUtils.trimToEmpty(propNameMap.get(col)));
                            msg.append("由[");
                            msg.append((String) oldVal);
                            msg.append("]");
                            msg.append("變為[");
                            msg.append((String) newVal);
                            msg.append("]");
                        }
                    } else if (newVal instanceof java.util.Date) {
                        // 日期
                        if (((java.util.Date) newVal).compareTo((java.util.Date) oldVal) != 0) {
                            msg.append(";");
                            msg.append(StringUtils.trimToEmpty(propNameMap.get(col)));
                            msg.append("由[");
                            msg.append(CapDate.formatDate((java.util.Date) oldVal, "YYYMMDD"));
                            msg.append("]");
                            msg.append("變為[");
                            msg.append(CapDate.formatDate((java.util.Date) newVal, "YYYMMDD"));
                            msg.append("]");
                        }
                    }
                }

            } catch (Exception e) {
                throw new CapMessageException("取得異動說明失敗", e, EloanUtils.class);
            }
        }

        String result = msg.toString();
        if (StringUtils.isNotBlank(result)) {
            result = result.substring(1);
        }
        return result;
    }

    public static <T extends DataObject> String getRemarkString(T src, T dest, Map<String, String> propNameMap) {
        return getRemarkString(src, dest, propNameMap, Collections.emptyMap());
    }

    public static <T extends DataObject> String getRemarkString(T src, T dest, Map<String, String> propNameMap, Map<String, Formatter> propValueFormatters) {
        if (null == src || null == dest) {
            return null;
        }
        StringBuilder msg = new StringBuilder(512);

        Class<? extends DataObject> dataClass = src.getClass();
        String[] cols = CapBeanUtil.getFieldName(dataClass, false);
        for (String col : cols) {
            // 不需比較欄位
            if (IGNORE_COLUMNS.contains(col) || col.endsWith("Oid")) {
                continue;
            }

            PropertyDescriptor pd;
            try {
                pd = new PropertyDescriptor(col, dataClass);
                Object newVal = pd.getReadMethod().invoke(dest);

                if (newVal != null && StringUtils.isNotBlank(newVal.toString())) {
                    msg.append(';');
                    msg.append(propNameMap.get(col));
                    msg.append(':');

                    Formatter fmt = propValueFormatters.get(col);
                    if (fmt != null) {
                        msg.append(fmt.reformat(newVal));
                    } else if (newVal instanceof java.util.Date) {
                        msg.append(CapDate.formatDate((java.util.Date) newVal, "YYY/MM/DD"));
                    } else {
                        msg.append(newVal);
                    }
                }
            } catch (Exception e) {
                throw new CapMessageException("取得異動說明失敗", e, EloanUtils.class);
            }
        }

        if (msg.length() > 0) {
            return msg.substring(1);
        }
        return "";
    }

    public static String getRandom(int len) {
        if (len <= 0) {
            return null;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < len; i++) {
            r.append(String.valueOf(new SecureRandom().nextInt(10)));
        }
        return r.toString();
    }

    /**
     * FILE PATH 過濾字串
     * 
     * @param path
     * @return
     */
    public static String cleanPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.length(); i++) {
                sb.append(cleanChar(path.charAt(i)));
            }
            return sb.toString();
        }
        return null;
    }
    private static char cleanChar(char c) {
        int ascii = c;
        if ((ascii >= 48 && ascii < 58) || (ascii >= 65 && ascii < 91) || (ascii >= 97 && ascii < 123)) {
            // 0-9, A-Z, a-z
            return c;
        }
        switch (c) {
        case '/':
        case '.':
        case '-':
        case '_':
        case ' ':
            return c;
        default:
            break;
        }
        return '%';
    }
}
