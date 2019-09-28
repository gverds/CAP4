/* 
 * Function.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.security.model;

import java.io.Serializable;

/**
 * <p>
 * 交易資料.
 * </p>
 * 
 * <pre>
 * $Date: 2010-09-28 12:08:43 +0800 (星期二, 28 九月 2010) $
 * $Author: iris $
 * $Revision: 656 $
 * $HeadURL: svn://192.168.0.1/MICB_ISDOC/cap/cap-core/src/main/java/tw/com/iisi/cap/security/model/IFunction.java $
 * </pre>
 *
 * @author iristu
 * @version $Revision: 656 $
 * @version
 *          <ul>
 *          <li>2010/7/26,iristu,new
 *          </ul>
 */
public interface Function extends Serializable {

    String getFuncUrl();

    String getFuncParent();

    int getFuncOrder();

}
