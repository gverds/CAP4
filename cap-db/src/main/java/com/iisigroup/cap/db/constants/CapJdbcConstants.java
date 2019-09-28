/* 
 * CapJdbcConstants.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.db.constants;

/**
 * <pre>
 * Constants for JDBC Template
 * </pre>
 * 
 * @since 2012/9/19
 * @author iristu
 * @version
 *          <ul>
 *          <li>2012/9/19,iristu,new
 *          </ul>
 */
public interface CapJdbcConstants {

    String SQL_QUERY_SUFFIX = "query.suffix";

    String SQL_PAGING_QUERY = "paging.querySql";

    String SQL_PAGING_TOTAL_PAGE = "paging.totalPage";

    String SQL_PAGING_SOURCE_SQL = "sourceSQL";

    String SQL_PAGING_SOURCE_ORDER = "sourceOrder";

    String SQL_PAGING_DUMMY_ORDER_BY = "order by TempColumn";
}
