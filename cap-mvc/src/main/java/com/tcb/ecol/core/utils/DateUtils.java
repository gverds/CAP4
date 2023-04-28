/* 
 * DateUtils.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.tcb.ecol.core.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.chrono.MinguoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iisigroup.cap.utils.CapDate;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2019年8月16日
 * @author Rudy
 * @version
 *          <ul>
 *          <li>2019年8月16日,Rudy,new
 *          </ul>
 */
public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * 取得今天日期，時間為00:00:00
     * 
     * @return
     */
    public static Date getCurrDate() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        return now.getTime();
    }

    /**
     * 日期處理(String+Format=Timestamp)
     * 
     * @param date
     * @return Timestamp
     */
    public static Timestamp convertStringToTimestampWithFormat(String date, String format) {
        if (StringUtils.isBlank(date)) {
            return null;
        }

        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setLenient(false);
        return new Timestamp(df.parse(date, new ParsePosition(0)).getTime());
    }

    /**
     * Timestamp轉為民國年格式yyy/MM/dd HH:mm:ss
     * 
     * @param date
     * @return
     */
    public static String convertDateTimeToTwDateTimeStr(Timestamp timestamp) {
        if (null == timestamp) {
            return null;
        }
        Chronology chrono = MinguoChronology.INSTANCE;
        DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern("yyy/MM/dd HH:mm:ss").toFormatter().withChronology(chrono)
                .withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
        LocalDateTime localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return df.format(localDateTime);
    }

    /**
     * 日期物件轉為民國年格式yyy/MM/dd
     * 
     * @param date
     * @return
     */
    public static String convertDateToTwDateStr(Date date) {
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
     * 日期物件轉為民國年格式: 110年01月01號
     * 
     * @param date
     * @return
     */
    public static String convertDateToTwDateStr2(Date date) {
        if (null == date) {
            return null;
        }
        Chronology chrono = MinguoChronology.INSTANCE;
        DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern("yyy/MM/dd").toFormatter().withChronology(chrono).withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
        Timestamp ts = new Timestamp(date.getTime());
        LocalDate localDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String twDate = df.format(localDate);
        StringBuffer result = new StringBuffer();
        if (StringUtils.isNotBlank(twDate)) {
            result.append(StringUtils.substring(twDate, 0, 3));
            result.append("年");
            result.append(StringUtils.substring(twDate, 4, 6));
            result.append("月");
            result.append(StringUtils.substring(twDate, 7, 9));
            result.append("日");
        }
        return result.toString();
    }
    
    /**
     * Timestamp增加日期 addDays(2021/01/14 12:05:01, 1) = 2021/01/15 12:05:01
     * 
     * @param timestamp
     * @param days
     * @return
     */
    public static Timestamp addDays(Timestamp date, int days) {
        if (null == date) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return new Timestamp(cal.getTime().getTime());
    }

    /**
     * Date增加日期
     * 
     * @param date
     * @param days
     * @return
     */
    public static Date addDays(Date date, int days) {
        if (null == date) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    /**
     * Date增加月份
     * 
     * @param date
     * @param days
     * @return
     */
    public static Date addMonths(Date date, int months) {
        if (null == date) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    /**
     * <pre>
     * 將字串轉為 Timestamp 
     * 僅支援民國年yyy/MM/dd HH:mm:ss以及西元年yyy/MM/dd HH:mm:ss HH:mm:ss
     * 傳入日期為空則回傳null 
     * 無法轉換時拋Exception
     * </pre>
     * 
     * @param twDate
     *            民國年yyy/MM/dd HH:mm:ss以及西元年yyy/MM/dd HH:mm:ss HH:mm:ss
     *
     * @return Timestamp
     */
    public static Timestamp convertStringToTimestamp(String timestamp) {
        Timestamp result = null;
        if (StringUtils.isNotBlank(timestamp)) {
            String yearStr = StringUtils.split(timestamp, "/")[0];
            if (Integer.parseInt(yearStr) >= 1000) {
                String format = "yyyy/MM/dd HH:mm:ss";
                result = convertStringToTimestampWithFormat(timestamp, format);
            } else {
                String format = "yyy/MM/dd HH:mm:ss";
                Chronology chrono = MinguoChronology.INSTANCE;
                DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern(format).toFormatter().withChronology(chrono).withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
                LocalDateTime localDateTime = LocalDateTime.parse(timestamp, df);
                result = Timestamp.valueOf(localDateTime);
            }
        }
        return result;
    }


    /**
     * <pre>
     * 將字串轉為 Timestamp 
     * 僅支援民國年yyy/MM/dd HH:mm:ss以及西元年yyy/MM/dd HH:mm:ss HH:mm:ss
     * 傳入日期為空則回傳null 
     * 無法轉換時拋Exception
     * </pre>
     * 
     * @param twDate
     *            民國年yyy/MM/dd HH:mm:ss以及西元年yyy/MM/dd HH:mm:ss HH:mm:ss
     *
     * @return Timestamp
     */
    public static Timestamp convertStringToTimestampWithoutTime(String timestamp) {
        Timestamp result = null;
        if (StringUtils.isNotBlank(timestamp)) {
            String yearStr = StringUtils.split(timestamp, "/")[0];
            if (Integer.parseInt(yearStr) >= 1000) {
                String format = "yyyy/MM/dd";
                result = convertStringToTimestampWithFormat(timestamp, format);
            } else {
                String format = "yyy/MM/dd";
                Chronology chrono = MinguoChronology.INSTANCE;
                DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern(format).toFormatter().withChronology(chrono).withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
                LocalDate localDate = LocalDate.parse(timestamp, df);
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (null != date) {
                    result = new Timestamp(date.getTime());
                }
            }
        }
        return result;
    }

    
    /**
     * <pre>
     * 將字串轉為 Date 
     * 僅支援民國年yyy/MM/dd以及西元年yyyy/MM/dd
     * 傳入日期為空則回傳null 
     * 無法轉換時拋Exception
     * </pre>
     * 
     * @param twDate
     *            yyy/MM/dd 3 位民國年、2 位月份、2 位日期
     * @return Date
     */
    public static Date convertStringToDate(String twDate) {
        Date result = null;
        if (StringUtils.isNotBlank(twDate)) {
            String yearStr = StringUtils.split(twDate, "/")[0];
            if (Integer.parseInt(yearStr) >= 1000) {
                // 西元年
                result = CapDate.parseDate(twDate);
            } else {
                // 民國年
                String format = "yyy/MM/dd";
                Chronology chrono = MinguoChronology.INSTANCE;
                DateTimeFormatter df = new DateTimeFormatterBuilder().parseLenient().appendPattern(format).toFormatter().withChronology(chrono).withDecimalStyle(DecimalStyle.of(Locale.getDefault()));
                // 避免timestamp轉換成data有問題
                LocalDate localDate = LocalDate.parse(twDate.split(" ")[0], df);
                result = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        }
        return result;
    }

    /**
     * 將民國年月日轉為西元年月日格式 傳入日期為回傳null 無法轉換時拋Exception 預設處理"yyy/mm/dd"與"yyy-mm-dd"，有特別分隔符號需傳入
     *
     * @param twDate
     *            yyy/MM/dd or yyy-MM-dd 3 位民國年、2 位月份、2 位日期
     * 
     * @return yyy/MM/dd
     */
    public static String convertTwDateStrToCeDateStr(String twDate) {
        String result = null;
        if (StringUtils.isNotBlank(twDate)) {
            Date date = convertStringToDate(twDate);
            if (null != date) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                result = df.format(date);
            }
        }
        return result;
    }
    

    public static Timestamp convertDateToTimestamp(Date date) {
        if (null == date) {
            return null;
        }
        return new Timestamp(date.getTime());
    }
    
    /**
     * 民國年月日加上分隔符號(yyyMMdd-->yyy/MM/dd)
     * @param sdate
     * @return String
     */
    public static String delimiterTWDate(String yyyMMdd, String delimiter) {    
        if(StringUtils.isBlank(yyyMMdd)){
            return "";
        }
        if(yyyMMdd.length() == 6)
            yyyMMdd = "0"+yyyMMdd;
        
        return yyyMMdd.substring(0, 3)+delimiter+yyyMMdd.substring(3, 5)+delimiter+yyyMMdd.substring(5);
    }   
    
    
    /**
     * 民國年月轉為西元年月
     * @param twYm
     * @return
     */
    public static String convertTwYmToCeYm(String twYm) {
        if (StringUtils.isBlank(twYm)) {
            return null;
        }
        // 去掉linkStr
        twYm = StringUtils.replace(twYm, "/", "");
        twYm = StringUtils.replace(twYm, "-", "");
        
        String year = "";
        String mon = "";
        
        // 四碼 ex:9801
        if (twYm.length() == 4) {
            year = String.valueOf(1911 + Integer.parseInt(twYm.substring(0,2)));
            mon = twYm.substring(2,4);
        }else if(twYm.length() == 5) {
            // 五碼: ex:11005
            year = String.valueOf(1911 + Integer.parseInt(twYm.substring(0,3)));
            mon = twYm.substring(3,5);
        }
        
        return year + mon;
    }
    
    /**
     * 西元年月轉為民國年月
     * @param twYm
     * @return
     */
    public static String convertCeYmToTwYm(String ceYm) {
        if (StringUtils.isBlank(ceYm)) {
            return null;
        }
        // 去掉linkStr
        ceYm = StringUtils.replace(ceYm, "/", "");
        ceYm = StringUtils.replace(ceYm, "-", "");
        
        String year = "";
        String mon = "";
        
        // 六碼 ex:202101
        if (ceYm.length() == 6) {
            year = String.valueOf(Integer.parseInt(ceYm.substring(0,4)) -1911);
            mon = ceYm.substring(4,6);
        }
        
        return year + mon;
    }
    
    /**
     * <pre>
     * 將西元年字串轉為Date,format預設yyyy/MM/dd
     * </pre>
     * 
     * @param twDate
     *            yyy/MM/dd 3 位民國年、2 位月份、2 位日期
     * @return Date
     */
    public static Date convertStringToDate(String date, String format) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            format = "yyyy/MM/dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date result = null;
        try {
            result = sdf.parse(date);
        } catch (ParseException e) {
            logger.error(e.toString());
        }
        return result;
    }
    
    /**
     * 取得當月最後一天
     * @param date
     * @return
     */
    public static Date getLastMonthDay(Date date) {
        if (null == date ) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        return calendar.getTime();
        
    }
    
}
