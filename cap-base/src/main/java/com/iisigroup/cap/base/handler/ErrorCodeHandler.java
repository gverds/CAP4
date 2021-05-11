/* 
 * ErrorCodeHandler.java
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.base.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;

import com.iisigroup.cap.annotation.HandlerType;
import com.iisigroup.cap.annotation.HandlerType.HandlerTypeEnum;
import com.iisigroup.cap.base.model.ErrorCode;
import com.iisigroup.cap.base.service.ErrorCodeService;
import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.Result;
import com.iisigroup.cap.component.impl.AjaxFormResult;
import com.iisigroup.cap.component.impl.BeanGridResult;
import com.iisigroup.cap.db.constants.SearchMode;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.model.Page;
import com.iisigroup.cap.db.service.CapCommonService;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.formatter.Formatter;
import com.iisigroup.cap.mvc.handler.MFormHandler;
import com.iisigroup.cap.security.CapSecurityContext;
import com.iisigroup.cap.utils.CapBeanUtil;
import com.iisigroup.cap.utils.CapDate;
import com.iisigroup.cap.utils.CapString;

/**
 * <pre>
 * 訊息代碼維護
 * </pre>
 * 
 * @since 2013/12/31
 * @author tammy
 * @version
 *          <ul>
 *          <li>2013/12/31,tammy,new
 *          </ul>
 */
@Controller("errorCodehandler")
public class ErrorCodeHandler extends MFormHandler {

    @Resource
    private CapCommonService commonSrv;

    @Resource
    private ErrorCodeService errorCodeService;

    @HandlerType(HandlerTypeEnum.GRID)
    public BeanGridResult query(SearchSetting search, Request params) {
        String code = params.get("code");
        String locale = params.get("locale");
        String sysId = params.get("sysId");

        if (!CapString.isEmpty(code)) {
            search.addSearchModeParameters(SearchMode.LIKE, "code", code);
        }
        if (!CapString.isEmpty(locale)) {
            search.addSearchModeParameters(SearchMode.EQUALS, "locale", locale);
        }
        if (!CapString.isEmpty(sysId)) {
            search.addSearchModeParameters(SearchMode.LIKE, "sysId", sysId);
        }
        search.addOrderBy("code");

        Map<String, Formatter> fmt = new HashMap<String, Formatter>();
        // fmt.put("lastModifyBy", new UserNameFormatter(this.userService));

        Page<ErrorCode> page = commonSrv.findPage(ErrorCode.class, search);
        return new BeanGridResult(page.getContent(), page.getTotalRow(), fmt);
    }

    /**
     * 編輯資料
     * 
     * @param request
     *            IRequest
     * @return {@link Result.com.iisi.cap.response.IResult}
     * @throws CapException
     */
    public Result save(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        String oid = request.get("oid");
        String code = request.get("code").toUpperCase();
        String locale = request.get("locale");
        ErrorCode errorCode = null;

        if (CapString.isEmpty(oid)) {
            errorCode = errorCodeService.getErrorCode(code, locale);
            if (errorCode != null) {
                result.set("exist", Boolean.TRUE);
                return result;
            }
        } else {
            errorCode = commonSrv.findById(ErrorCode.class, oid);
        }

        if (errorCode == null) {
            errorCode = new ErrorCode();
            errorCode.setOid(null);
        }
        CapBeanUtil.map2Bean(request, errorCode, ErrorCode.class);
        errorCode.setCode(errorCode.getCode().toUpperCase());
        errorCode.setLastModifyBy(CapSecurityContext.getUserId());
        errorCode.setLastModifyTime(CapDate.getCurrentTimestamp());
        errorCodeService.save(errorCode);

        return result;
    }

    /**
     * 刪除資料
     * 
     * @param request
     *            IRequest
     * @return {@link Result.com.iisi.cap.response.IResult}
     * @throws CapException
     */
    public Result delete(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        ErrorCode code = commonSrv.findById(ErrorCode.class, request.get("oid"));
        if (code != null) {
            commonSrv.delete(code);
        }
        return result;
    }

}
