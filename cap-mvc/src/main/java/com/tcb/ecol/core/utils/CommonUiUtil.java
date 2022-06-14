/* 
 * CommonUiUtil.java
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

import java.io.BufferedReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iisigroup.cap.model.GenericBean;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * 前端共用元件對應資料處理的 Utility Class
 * </pre>
 * 
 * @since 2019年7月26日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2019年7月26日,Lancelot,new
 *          </ul>
 */
public class CommonUiUtil {
	
    private static final Logger informer = LoggerFactory.getLogger(CommonUiUtil.class);

    // 住址
    public enum AddressKey {
        ZIP("zipCode"),
        CITY("cityCode"),
        AREA("townCode"),
        VILLAGE("addrVillage"),
        VILZONE("addrVilzone"),
        ROAD("addrRoad"),
        RCODE("addrRcode"),
        SEC("addrSec"),
        LANE("addrLane"),
        ALLEY("addrAlley"),
        NO("addrNo"),
        NO2("addrNo2"),
        FL("addrFl"),
        FLD("addrFld"),
        ROOM("addrRoom");

        String code;

        AddressKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 公告現值
    public enum AmtKey {
        AMT("amt"),
        AMTP("amtp");

        String code;

        AmtKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 建物總面積
    public enum AreaKey {
        M2("m2"),
        P("p");

        String code;

        AreaKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 建號
    public enum BldnoKey {
        BLDNO1("bldno1"),
        BLDNO2("bldno2");

        String code;

        BldnoKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 徵信報告號碼
    public enum CrdtnoKey {
        BRCHID("brchid"),
        ACCT("acct"),
        CRDTNO("crdtno");

        String code;

        CrdtnoKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 員工
    public enum EmpKey {
        EMPID("empId"),
        EMPNAME("empName");

        String code;

        EmpKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 統一編號
    public enum IdnKey {
        IDN("idn"),
        DUP("dup");

        String code;

        IdnKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 地號
    public enum LandnoKey {
        LANDNO1("landno1"),
        LANDNO2("landno2");

        String code;

        LandnoKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 坐落
    public enum LocationKey {
        ZIP("zip"),
        CITY("city"),
        AREA("area"),
        SECTION("section");

        String code;

        LocationKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // 連絡電話
    public enum TelKey {
        AREA("area"),
        NUM("num"),
        EXT("ext");

        String code;

        TelKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 將目前共用元件serializeData的jsonStr，依據beanPrefix to fit selma
     * 
     * @param jsonStr
     *            共用元件serializeData的jsonStr
     * @param beanPrefix
     *            fit selma 的關鍵字 idn、ban
     * @return
     */
    public static Map<String, Object> getIdnUiStr2BeanMap(String jsonStr, String beanPrefix) {
        Map<String, Object> idnMap = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        for (IdnKey idnKey : IdnKey.values()) {
            String idnCod = idnKey.getCode();
            String col = getIdnCol(beanPrefix, idnCod);
            idnMap.put(col, fromJson.get(idnCod));
        }
        return idnMap;
    }

    // 依據現行關於idn slema的規則
    private static String getIdnCol(String prefix, String col) {
        if (!CapString.isEmpty(prefix)) {
            String firstWord = col.substring(0, 1).toUpperCase();
            String secWord = col.substring(1, col.length());
            col = prefix + firstWord + secWord;
            if (col.indexOf("Idn") != -1) {
                col = col.replace("Idn", "");
            }
        }
        return col;

    }

    /**
     * 將目前selma的欄位，透過beanPrefix並依據uiId建立共用元件所需的格式
     * 
     * @param map
     *            資料來源
     * @param uiId
     *            共用元件id名稱
     * @param beanPrefix
     *            轉換成selma的關鍵字 idn、ban
     * @return 可以直接使用的資料
     */
    public static Map<String, Object> getIdnBeanMap2UiMap(Map<String, Object> map, String uiId, String beanPrefix) {
        Map<String, Object> uiMap = new HashMap<>();
        Map<String, Object> idnMap = new HashMap<>();
        for (IdnKey idnKey : IdnKey.values()) {
            String locationCod = idnKey.getCode();
            String col = getIdnCol(beanPrefix, locationCod);
            Object value = map.get(col);
            if (value != null && !CapString.isEmpty(value.toString())) {
                idnMap.put(locationCod, value);
                map.remove(col);
            }
        }
        uiMap.put(uiId, idnMap);
        map.putAll(uiMap);
        return map;
    }

    /**
     * 將目前共用元件serializeData的jsonStr，依據beanPrefix to fit selma
     * 
     * @param jsonStr
     *            共用元件serializeData的jsonStr
     * @param beanPrefix
     *            fit selma 的關鍵字 regTel、resTel、banTel
     * @return
     */
    public static Map<String, Object> getLocationUiStr2BeanMap(String jsonStr, String beanPrefix) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        for (LocationKey locationKey : LocationKey.values()) {
            String telCod = locationKey.getCode();
            String col = getLocationCol(beanPrefix, telCod);
            if (StringUtils.isNotBlank(col)) {
                map.put(col, fromJson.get(telCod));
            }
        }
        return map;
    }

    /**
     * 將目前共用元件serializeData的jsonStr，依據beanPrefix to fit selma
     * 
     * @param jsonStr
     *            共用元件serializeData的jsonStr
     * @param beanPrefix
     *            fit selma 的關鍵字 regTel、resTel、banTel
     * @return
     */
    public static Map<String, Object> getTelUiStr2BeanMap(String jsonStr, String beanPrefix) {
        Map<String, Object> telMap = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        for (TelKey telKey : TelKey.values()) {
            String addressCod = telKey.getCode();
            String col = getTelCol(beanPrefix, addressCod);
            telMap.put(col, fromJson.get(addressCod));
        }
        return telMap;
    }

    // 依據現行關於location slema的規則
    private static String getLocationCol(String prefix, String col) {
        String c = "";
        switch (col) {
        case "zip":
            c = "zipCode";
            break;
        case "city":
            c = "cityCode";
            break;
        case "area":
            c = "townCode";
            break;
        }
        return c;
    }

    // 依據現行關於tel slema的規則
    private static String getTelCol(String prefix, String col) {
        if (!CapString.isEmpty(prefix)) {
            String firstWord = col.substring(0, 1).toUpperCase();
            String secWord = col.substring(1, col.length());
            col = prefix + firstWord + secWord;
            if (col.indexOf("TelNum") != -1) {
                col = col.replace("TelNum", "Tel");
            }
        }
        return col;
    }

    /**
     * 將目前selma的欄位，透過beanPrefix並依據uiId建立共用元件所需的格式
     * 
     * @param map
     *            資料來源
     * @param uiId
     *            共用元件id名稱
     * @param beanPrefix
     *            轉換成selma的關鍵字 regTel、resTel、banTel
     * @return 可以直接使用的資料
     */
    public static Map<String, Object> getTelBeanMap2UiMap(Map<String, Object> map, String uiId, String beanPrefix) {
        Map<String, Object> uiMap = new HashMap<>();
        Map<String, Object> telMap = new HashMap<>();
        for (TelKey telKey : TelKey.values()) {
            String key = telKey.getCode();
            String col = getTelCol(beanPrefix, key);
            Object value = map.get(col);
            if (value != null && !CapString.isEmpty(value.toString())) {
                telMap.put(key, value);
                map.remove(col);
            }
        }
        uiMap.put(uiId, telMap);
        map.putAll(uiMap);
        return map;
    }

    /**
     * 將目前共用元件serializeData的jsonStr，依據beanPrefix to fit selma
     * 
     * @param jsonStr
     *            共用元件serializeData的jsonStr
     * @param beanPrefix
     *            fit selma 的關鍵字 regAddress、resAddress、banAddress
     * @return
     */
    public static Map<String, Object> getAddressUiStr2BeanMap(String jsonStr, String beanPrefix) {
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        return getAddressUiStr2BeanMap(fromJson,beanPrefix);
    }
    
    public static Map<String, Object> getAddressUiStr2BeanMap(Map<String, Object> fromJson, String beanPrefix) {
        Map<String, Object> addressMap = new HashMap<>();
        for (AddressKey addressKK : AddressKey.values()) {
            String addressCod = addressKK.getCode();
            String col = getAddressCol2(beanPrefix, addressCod);
            addressMap.put(col, fromJson.get(addressCod));
        }
        return addressMap;
    }

    // 依據現行關於address slema的規則
    private static String getAddressCol2(String prefix, String col) {
        if (!CapString.isEmpty(prefix)) {
            String firstWord = col.substring(0, 1).toUpperCase();
            String secWord = col.substring(1, col.length());
            prefix = prefix.replace("Address", "");
            col = prefix + firstWord + secWord;
        }
        return col;
    }

    /**
     * 將目前selma的欄位，透過beanPrefix並依據uiId建立共用元件所需的格式
     * 
     * @param map
     *            資料來源
     * @param uiId
     *            共用元件id名稱
     * @param beanPrefix
     *            轉換成selma的關鍵字 regAddress、resAddress、banAddress
     * @return 可以直接使用的資料
     */
    public static Map<String, Object> getAddressBeanMap2UiMap(Map<String, Object> map, String uiId, String beanPrefix) {
        Map<String, Object> uiMap = new HashMap<>();
        Map<String, Object> addressMap = new HashMap<>();
        for (AddressKey addressKey : AddressKey.values()) {
            String key = addressKey.getCode();
            String col = getAddressCol2(beanPrefix, key);
            Object value = map.get(col);
            if (value != null && !CapString.isEmpty(value.toString())) {
                addressMap.put(key, value);
                map.remove(col);
            }
        }
        uiMap.put(uiId, addressMap);
        map.putAll(uiMap);
        return map;
    }

    /**
     * 取得回傳給住址共用元件的資料
     * 
     * @param uiId
     * @param zip
     * @param city
     * @param area
     * @param village
     * @param vilzone
     * @param road
     * @param rcode
     * @param sec
     * @param lane
     * @param alley
     * @param no
     * @param no2
     * @param fl
     * @param fld
     * @param room
     * @return
     */
    public static Map<String, Object> getAddressResult(String uiId, String zip, String city, String area, String village, String vilzone, String road, String rcode, String sec, String lane,
            String alley, String no, String no2, String fl, String fld, String room) {

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put(AddressKey.ZIP.getCode(), zip);
        addressMap.put(AddressKey.CITY.getCode(), city);
        addressMap.put(AddressKey.AREA.getCode(), area);
        addressMap.put(AddressKey.VILLAGE.getCode(), village);
        addressMap.put(AddressKey.VILZONE.getCode(), vilzone);
        addressMap.put(AddressKey.ROAD.getCode(), road);
        addressMap.put(AddressKey.RCODE.getCode(), rcode);
        addressMap.put(AddressKey.SEC.getCode(), sec);
        addressMap.put(AddressKey.LANE.getCode(), lane);
        addressMap.put(AddressKey.ALLEY.getCode(), alley);
        addressMap.put(AddressKey.NO.getCode(), no);
        addressMap.put(AddressKey.NO2.getCode(), no2);
        addressMap.put(AddressKey.FL.getCode(), fl);
        addressMap.put(AddressKey.FLD.getCode(), fld);
        addressMap.put(AddressKey.ROOM.getCode(), room);

        result.put(uiId, addressMap);
        return result;
    }

    /**
     * 將前端傳入的住址資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param cityCol
     * @param areaCol
     * @param sectionCol
     * @return
     */
    public static Map<String, Object> getAddressForModel(String jsonStr, String zipCol, String cityCol, String areaCol, String villageCol, String vilzoneCol, String roadCol, String rcodeCol,
            String secCol, String laneCol, String alleyCol, String noCol, String no2Col, String flCol, String fldCol, String roomCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(zipCol, fromJson.get(AddressKey.ZIP.getCode()));
        result.put(cityCol, fromJson.get(AddressKey.CITY.getCode()));
        result.put(areaCol, fromJson.get(AddressKey.AREA.getCode()));
        result.put(villageCol, fromJson.get(AddressKey.VILLAGE.getCode()));
        result.put(vilzoneCol, fromJson.get(AddressKey.VILZONE.getCode()));
        result.put(roadCol, fromJson.get(AddressKey.ROAD.getCode()));
        result.put(rcodeCol, fromJson.get(AddressKey.RCODE.getCode()));
        result.put(secCol, fromJson.get(AddressKey.SEC.getCode()));
        result.put(laneCol, fromJson.get(AddressKey.LANE.getCode()));
        result.put(alleyCol, fromJson.get(AddressKey.ALLEY.getCode()));
        result.put(noCol, fromJson.get(AddressKey.NO.getCode()));
        result.put(no2Col, fromJson.get(AddressKey.NO2.getCode()));
        result.put(flCol, fromJson.get(AddressKey.FL.getCode()));
        result.put(fldCol, fromJson.get(AddressKey.FLD.getCode()));
        result.put(roomCol, fromJson.get(AddressKey.ROOM.getCode()));
        return result;
    }

    /**
     * 
     * @param uiId
     * @param prefix
     * @param bean
     * @return
     */
    public static Map<String, Object> getAddressFromModel(String uiId, String prefix, GenericBean bean) {
        // 根據 prefix + AddressKey 取得所有地址相關欄位
        // String[] fields;
        // 透過 reflection 從 bean 取得所有地址相關的值
        // 塞入回傳的 map
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        String col = getAddressCol(prefix, AddressKey.ZIP.getCode());
        String key = (CapString.isEmpty(uiId)) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.CITY.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.AREA.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.VILLAGE.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.VILZONE.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.ROAD.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.RCODE.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.SEC.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.LANE.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.ALLEY.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.NO.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.NO2.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.FL.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.FLD.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        col = getAddressCol(prefix, AddressKey.ROOM.getCode());
        key = (uiId == null) ? col : uiId + "_" + col;
        map.put(key, bean.get(col));
        return map;
    }

    private static String getAddressCol(String prefix, String col) {
        if (!CapString.isEmpty(prefix)) {
            String firstWord = col.substring(0, 1).toUpperCase();
            String secWord = col.substring(1, col.length());
            col = prefix + firstWord + secWord;
        }
        return col;
    }

    /**
     * 取得回傳給公告現值共用元件的資料
     * 
     * @param uiId
     * @param amt
     * @param amtp
     * @return
     */
    public static Map<String, Object> getAmtResult(String uiId, String amt, String amtp) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> amtMap = new HashMap<>();
        amtMap.put(AmtKey.AMT.getCode(), amt);
        amtMap.put(AmtKey.AMTP.getCode(), amtp);

        result.put(uiId, amtMap);
        return result;
    }

    /**
     * 將前端傳入的公告現值資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param amtCol
     * @param amtpCol
     * @return
     */
    public static Map<String, Object> getAmtForModel(String jsonStr, String amtCol, String amtpCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(amtCol, fromJson.get(AmtKey.AMT.getCode()));
        result.put(amtpCol, fromJson.get(AmtKey.AMTP.getCode()));
        return result;
    }

    /**
     * 取得回傳給建物總面積共用元件的資料
     * 
     * @param uiId
     * @param m2
     * @param p
     * @return
     */
    public static Map<String, Object> getAreaResult(String uiId, String m2, String p) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> areaMap = new HashMap<>();
        areaMap.put(AreaKey.M2.getCode(), m2);
        areaMap.put(AreaKey.P.getCode(), p);

        result.put(uiId, areaMap);
        return result;
    }

    /**
     * 將前端傳入的建物總面積資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param m2Col
     * @param pCol
     * @return
     */
    public static Map<String, Object> getAreaForModel(String jsonStr, String m2Col, String pCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(m2Col, fromJson.get(AreaKey.M2.getCode()));
        result.put(pCol, fromJson.get(AreaKey.P.getCode()));
        return result;
    }

    /**
     * 取得回傳給徵信報告號碼共用元件的資料
     * 
     * @param uiId
     * @param brchid
     * @param acct
     * @param crdtno
     * @return
     */
    public static Map<String, Object> getCrdtnoResult(String uiId, String brchid, String acct, String crdtno) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> crdtnoMap = new HashMap<>();
        crdtnoMap.put(CrdtnoKey.BRCHID.getCode(), brchid);
        crdtnoMap.put(CrdtnoKey.ACCT.getCode(), acct);
        crdtnoMap.put(CrdtnoKey.CRDTNO.getCode(), crdtno);

        result.put(uiId, crdtnoMap);
        return result;
    }

    /**
     * 將前端傳入的徵信報告號碼資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param brchidCol
     * @param acctCol
     * @param crdtnoCol
     * @return
     */
    public static Map<String, Object> getCrdtnoForModel(String jsonStr, String brchidCol, String acctCol, String crdtnoCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(brchidCol, fromJson.get(CrdtnoKey.BRCHID.getCode()));
        result.put(acctCol, fromJson.get(CrdtnoKey.ACCT.getCode()));
        result.put(crdtnoCol, fromJson.get(CrdtnoKey.CRDTNO.getCode()));

        return result;
    }

    /**
     * 取得回傳給員工共用元件的資料
     * 
     * @param uiId
     * @param empId
     * @param empName
     * @return
     */
    public static Map<String, Object> getEmpResult(String uiId, String empId, String empName) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> empMap = new HashMap<>();
        empMap.put(EmpKey.EMPID.getCode(), empId);
        empMap.put(EmpKey.EMPNAME.getCode(), empName);

        result.put(uiId, empMap);
        return result;
    }

    /**
     * 將前端傳入的員工資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param empIdCol
     * @param empNameCol
     * @return
     */
    public static Map<String, Object> getEmpForModel(String jsonStr, String empIdCol, String empNameCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(empIdCol, fromJson.get(EmpKey.EMPID.getCode()));
        result.put(empNameCol, fromJson.get(EmpKey.EMPNAME.getCode()));

        return result;
    }

    /**
     * 取得回傳給統一編號共用元件的資料
     * 
     * @param uiId
     * @param idn
     * @param dup
     * @return
     */
    public static Map<String, Object> getIdnResult(String uiId, String idn, String dup) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> idnMap = new HashMap<>();
        idnMap.put(IdnKey.IDN.getCode(), idn);
        idnMap.put(IdnKey.DUP.getCode(), dup);

        result.put(uiId, idnMap);
        return result;
    }

    /**
     * 將前端傳入的統一編號資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param idnCol
     * @param dupCol
     * @return
     */
    public static Map<String, Object> getIdnForModel(String jsonStr, String idnCol, String dupCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(idnCol, fromJson.get(IdnKey.IDN.getCode()));
        result.put(dupCol, fromJson.get(IdnKey.DUP.getCode()));

        return result;
    }

    /**
     * 取得回傳給座落共用元件的資料
     * 
     * @param uiId
     * @param city
     * @param area
     * @param section
     * @return
     */
    public static Map<String, Object> getLocationResult(String uiId, String city, String area, String section) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put(LocationKey.CITY.getCode(), city);
        locationMap.put(LocationKey.AREA.getCode(), area);
        locationMap.put(LocationKey.SECTION.getCode(), section);

        result.put(uiId, locationMap);
        return result;
    }

    /**
     * 將前端傳入的座落資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param cityCol
     * @param areaCol
     * @param sectionCol
     * @return
     */
    public static Map<String, Object> getLocationForModel(String jsonStr, String cityCol, String areaCol, String sectionCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(cityCol, fromJson.get(LocationKey.CITY.getCode()));
        result.put(areaCol, fromJson.get(LocationKey.AREA.getCode()));
        result.put(sectionCol, fromJson.get(LocationKey.SECTION.getCode()));
        return result;
    }

    /**
     * 取得回傳給連絡電話共用元件的資料
     * 
     * @param uiId
     * @param area
     * @param num
     * @param ext
     * @return
     */
    public static Map<String, Object> getTelResult(String uiId, String area, String num, String ext) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> telMap = new HashMap<>();
        telMap.put(TelKey.AREA.getCode(), area);
        telMap.put(TelKey.NUM.getCode(), num);
        telMap.put(TelKey.EXT.getCode(), ext);

        result.put(uiId, telMap);
        return result;
    }

    /**
     * 將前端傳入的連絡電話資料(json string)，依據指定的名稱重新組成 Map
     * 
     * @param jsonStr
     * @param areaCol
     * @param numCol
     * @param extCol
     * @return
     */
    public static Map<String, Object> getTelForModel(String jsonStr, String areaCol, String numCol, String extCol) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> fromJson = GsonUtil.jsonToMap(jsonStr);
        result.put(areaCol, fromJson.get(TelKey.AREA.getCode()));
        result.put(numCol, fromJson.get(TelKey.NUM.getCode()));
        result.put(extCol, fromJson.get(TelKey.EXT.getCode()));
        return result;
    }

}
