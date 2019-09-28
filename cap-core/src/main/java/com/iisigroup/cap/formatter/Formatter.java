/* 
 * Formatter.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.formatter;

import java.io.Serializable;

/**
 * <p>
 * reformat 欄位值
 * </p>
 * 
 * @author iristu
 * @version
 *          <ul>
 *          <li>2010/1/8,iristu,new
 *          <li>2011/11/1,rodeschen,from cap
 *          </ul>
 */
public interface Formatter extends Serializable {

    /**
     * 重新format傳入的值
     * 
     * @param in
     * @return
     */
    public String reformat(Object in);

}
