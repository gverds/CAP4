/* 
 * CustomStandardMultipartHttpServletRequest.java
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. ("Confidential Information").
 */
package com.iisigroup.cap.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <pre>
 * 擴充 StandardMultipartHttpServletRequest，補足非 file 欄位 value 的快取。
 *
 * 背景：Spring 6 移除 CommonsMultipartResolver 後，
 * StandardMultipartHttpServletRequest.parseRequest() 對非 file Part
 * 只記錄 name（multipartParameterNames），value 完全依賴底層 servlet container
 * 的 getParameter() 回傳。若 container 未正確實作此行為，參數值將遺失。
 * 本類別在建構時主動讀取所有非 file Part 的 value 並快取，
 * 以對齊舊版 CommonsFileUploadSupport.parseFileItems() 的行為。
 * </pre>
 *
 * @since 2025/03/24
 * @author iristu
 */
public class CustomStandardMultipartHttpServletRequest
        extends StandardMultipartHttpServletRequest {

    private final Map<String, String[]> multipartParameters = new LinkedHashMap<>();

    /**
     * 建立 CustomStandardMultipartHttpServletRequest 並立即解析 multipart 參數。
     *
     * @param request the servlet request to wrap
     * @throws MultipartException if parsing failed
     */
    public CustomStandardMultipartHttpServletRequest(
            HttpServletRequest request) throws MultipartException {
        super(request);
        parseParameters(request);
    }

    private void parseParameters(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        Charset charset = (encoding != null) ? Charset.forName(encoding) : StandardCharsets.UTF_8;

        try {
            Collection<Part> parts = request.getParts();
            Map<String, List<String>> temp = new LinkedHashMap<>();

            for (Part part : parts) {
                // 只處理非 file 欄位（file Part 由父類別的 MultipartFile 機制負責）
                if (part.getSubmittedFileName() == null) {
                    String value = new String(
                            part.getInputStream().readAllBytes(),
                            charset);
                    temp.computeIfAbsent(part.getName(), k -> new ArrayList<>()).add(value);
                }
            }

            temp.forEach((key, values) -> multipartParameters.put(key, values.toArray(new String[0])));

        } catch (IOException e) {
            throw new MultipartException("Failed to parse multipart parameters", e);
        } catch (MultipartException e) {
            throw e;
        } catch (Exception e) {
            throw new MultipartException("Failed to parse multipart request", e);
        }
    }

    @Override
    public String getParameter(String name) {
        String[] values = multipartParameters.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = multipartParameters.get(name);
        if (values != null) {
            return values;
        }
        return super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new LinkedHashMap<>(super.getParameterMap());
        result.putAll(multipartParameters);
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> superNames = super.getParameterNames();
        while (superNames.hasMoreElements()) {
            names.add(superNames.nextElement());
        }
        names.addAll(multipartParameters.keySet());
        return Collections.enumeration(names);
    }
}
