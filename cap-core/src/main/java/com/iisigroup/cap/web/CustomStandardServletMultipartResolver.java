/* 
 * CustomStandardServletMultipartResolver.java
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

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * <pre>
 * 擴充 StandardServletMultipartResolver，回傳 CustomStandardMultipartHttpServletRequest
 * 以補足非 file 欄位 value 的快取，對齊舊版 CommonsFileUploadSupport.parseFileItems() 行為。
 * </pre>
 *
 * @since 2025/03/24
 * @author iristu
 */
public class CustomStandardServletMultipartResolver
        extends StandardServletMultipartResolver {

    @Override
    public MultipartHttpServletRequest resolveMultipart(
            HttpServletRequest request) throws MultipartException {
        return new CustomStandardMultipartHttpServletRequest(request);
    }
}
