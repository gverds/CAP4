/* 
 * DataTablesAjaxFormResult.java
 * 
 * Copyright (c) 2009-2017 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.component.impl;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2017年5月25日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2017年5月25日,Lancelot,new
 *          </ul>
 */
public class DataTablesAjaxFormResult extends AjaxFormResult {
    public AjaxFormResult set(String key, List<Map<String, Object>> val, String dummy) {
        resultMap.put(key, val);
        return this;
    }
}
