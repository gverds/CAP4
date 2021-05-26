/* 
 * SqlTimestampTypeAdapter.java
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

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.MinguoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * <pre>
 * java.sql.Timestamp 類別的 Gson Type Adapter，依據 com.google.gson.internal.bind.SqlDateTypeAdapter 改寫
 * </pre>
 * 
 * @since 2019年7月2日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2019年7月2日,Lancelot,new
 *          </ul>
 */
public final class MinguoTimestampTypeAdapter extends TypeAdapter<Timestamp> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return typeToken.getRawType() == Timestamp.class ? (TypeAdapter<T>) new MinguoTimestampTypeAdapter() : null;
        }
    };

    private final DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public synchronized Timestamp read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        Timestamp result = CapDate.convertTwDateStrToTimestamp(in.nextString());
		return result;
    }

    @Override
    public synchronized void write(JsonWriter out, Timestamp value) throws IOException {
        out.value(value == null ? null : CapDate.convertDateTimeToTwDateTimeStr(value));
    }
    
}
