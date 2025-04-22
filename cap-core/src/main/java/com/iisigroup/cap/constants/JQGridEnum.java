/**
 * IGridEnum.java
 *
 * Copyright (c) 2009 International Integrated System, Inc.
 * 11F, No.133, Sec.4, Minsheng E. Rd., Taipei, 10574, Taiwan, R.O.C.
 * All Rights Reserved.
 *
 * Licensed Materials - Property of International Integrated System,Inc.
 *
 * This software is confidential and proprietary information of
 * International Integrated System, Inc. ("Confidential Information").
*/
package com.iisigroup.cap.constants;

/**
 * <p>
 * IGridEnum
 * </p>
 * 
 *
 * @author Tim
 * @version
 *          <ul>
 *          <li>2025/04/22,timchiang,new
 *          </ul>
 */
public enum JQGridEnum {
    PAGE("page"),
    PAGEROWS("rows"),
    TOTAL("total"),
    RECORDS("records"),
    SORTTYPE("sord"),
    SORTASC("asc"),
    SORTDESC("desc"),
    SORTCOLUMN("sidx"),
    CELL("cell"),
    COL_NAME("name"),
    COL_INDEX("index"),
    COL_PARAM("_columnParam"),
    START("start");

    private String code;

    JQGridEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

}
