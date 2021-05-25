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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.MinguoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
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
public final class MinguoDateTypeAdapter extends TypeAdapter<Date> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return typeToken.getRawType() == Timestamp.class ? (TypeAdapter<T>) new MinguoDateTypeAdapter() : null;
        }
    };

    private final DateFormat format = new SimpleDateFormat("yyyy/MM/dd");

    @Override
    public synchronized Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        final long utilDate = convertTwDateToTimestamp(in.nextString(), null).getTime();
		return new Date(utilDate);
    }
    
    @Override 
    public synchronized void write(JsonWriter out, Date value) throws IOException {
        out.value(value == null ? null : convertDateToTwDate(value));
    }
    
    /**
     * 日期物件轉為民國年格式yyy/MM/dd
     * 
     * @param date
     * @return
     */
    private String convertDateToTwDate(Date date) {
        if (null == date) {
            return null;
        }
        Chronology chrono = MinguoChronology.INSTANCE;
        DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern("yyy/MM/dd").toFormatter().withChronology(chrono).withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
        Timestamp ts = new Timestamp(date.getTime());
        LocalDate localDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return df.format(localDate);
    }
    

	/**
	 * 將民國年月日轉為 Timestamp，
	 * 傳入日期為回傳null
	 * 無法轉換時拋Exception
	 * 預設處理"yyy/mm/dd"與"yyy-mm-dd"，有特別分隔符號需傳入
	 *
	 * @param twDate yyy/MM/dd 3 位民國年、2 位月份、2 位日期
	 * @return
	 */
	private Date convertTwDateToTimestamp(String twDate, String format) {
		Date result = null;
		if (StringUtils.isNotBlank(twDate)) {

			if (StringUtils.isBlank(format)) {
				format = "yyy/MM/dd";
			}

	        Chronology chrono = MinguoChronology.INSTANCE;
	        DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern(format).toFormatter().withChronology(chrono)
	                .withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
	        
	        LocalDate localDate = LocalDate.parse(twDate, df);
	        result = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		return result;
	}
}
