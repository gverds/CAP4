/* 
 * NumberTypeAdapter.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.utils;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * <pre>
 * java.lang.Number 類別的 Gson Type Adapter，只處理數值轉 json 時，一律轉成字串。
 * </pre>
 * 
 * @since 2019年7月2日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2019年7月2日,Lancelot,new
 *          </ul>
 */
public final class NumberTypeAdapter implements JsonSerializer<Number> {

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Number src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(String.valueOf(src));
    }
}
