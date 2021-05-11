/* 
 * PgmSetHandler.java
 * 
 * Copyright (c) 2009-2012 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.auth.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.iisigroup.cap.annotation.HandlerType;
import com.iisigroup.cap.annotation.HandlerType.HandlerTypeEnum;
import com.iisigroup.cap.auth.model.DefaultFunction;
import com.iisigroup.cap.auth.model.RoleFunction;
import com.iisigroup.cap.auth.service.FunctionSetService;
import com.iisigroup.cap.base.formatter.impl.CodeTypeFormatter;
import com.iisigroup.cap.base.service.CodeTypeService;
import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.Result;
import com.iisigroup.cap.component.impl.AjaxFormResult;
import com.iisigroup.cap.component.impl.BeanGridResult;
import com.iisigroup.cap.component.impl.MapGridResult;
import com.iisigroup.cap.db.constants.SearchMode;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.model.Page;
import com.iisigroup.cap.db.service.CapCommonService;
import com.iisigroup.cap.db.utils.CapEntityUtil;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.exception.CapMessageException;
import com.iisigroup.cap.formatter.Formatter;
import com.iisigroup.cap.mvc.handler.MFormHandler;
import com.iisigroup.cap.security.CapSecurityContext;
import com.iisigroup.cap.utils.CapAppContext;
import com.iisigroup.cap.utils.CapDate;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * 系統功能維護
 * </pre>
 * 
 * @since 2014/1/16
 * @author tammy
 * @version
 *          <ul>
 *          <li>2014/1/16,tammy,new
 *          </ul>
 */
@Controller("functionsethandler")
public class FunctionSetHandler extends MFormHandler {

    @Resource
    private CapCommonService commonSrv;

    @Autowired
    private CodeTypeService codeTypeService;

    @Resource
    private FunctionSetService functionSetService;

    @HandlerType(HandlerTypeEnum.GRID)
    public BeanGridResult query(SearchSetting search, Request params) {
        String sysType = params.get("sysType");
        String level = params.get("level");
        String code = params.get("code");
        String name = params.get("name");

        if (!CapString.isEmpty(sysType)) {
            search.addSearchModeParameters(SearchMode.EQUALS, "sysType", sysType);
        }
        if (!CapString.isEmpty(level)) {
            search.addSearchModeParameters(SearchMode.EQUALS, "level", level);
        }
        if (!CapString.isEmpty(code)) {
            search.addSearchModeParameters(SearchMode.EQUALS, "code", code);
        }
        if (!CapString.isEmpty(name)) {
            search.addSearchModeParameters(SearchMode.EQUALS, "name", name);
        }

        Map<String, Formatter> fmt = new HashMap<String, Formatter>();
        fmt.put("SYSNAME", new CodeTypeFormatter(codeTypeService, "authSysId"));

        Page<DefaultFunction> page = commonSrv.findPage(DefaultFunction.class, search);
        return new BeanGridResult(page.getContent(), page.getTotalRow(), fmt);
    }

    @HandlerType(HandlerTypeEnum.GRID)
    public MapGridResult queryRole(SearchSetting search, Request params) {
        String sysType = params.get("sysType");
        String code = params.get("code");
        if (CapString.isEmpty(code)) {
            return new MapGridResult();
        }

        Page<Map<String, Object>> page = functionSetService.findPage(search, sysType, code);
        return new MapGridResult(page.getContent(), page.getTotalRow(), null);
    }

    @HandlerType(HandlerTypeEnum.GRID)
    public MapGridResult queryAllRole(SearchSetting search, Request params) {
        String sysType = params.get("sysType");
        String code = params.get("code");
        if (CapString.isEmpty(code)) {
            return new MapGridResult();
        }

        Page<Map<String, Object>> page = functionSetService.findEditPage(search, sysType, code);
        return new MapGridResult(page.getContent(), page.getTotalRow(), null);
    }

    public Result queryForm(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        String code = request.get("code");
        DefaultFunction function = null;

        if (!CapString.isEmpty(code)) {
            function = functionSetService.findFunctionByCode(code);
        }

        if (function != null) {
            result.putAll(new AjaxFormResult(function.toJSONObject(CapEntityUtil.getColumnName(function), null)));
        }

        return result;
    }

    public Result loadFunc(Request request) throws CapException {
        AjaxFormResult result = new AjaxFormResult();
        String sysType = request.get("sysType");
        String level = request.get("level");

        List<DefaultFunction> functions = functionSetService.findFunctionBySysTypeAndLevel(sysType, level);

        if (!CollectionUtils.isEmpty(functions)) {
            List<Map<String, Object>> funcArray = new ArrayList<Map<String, Object>>();
            for (DefaultFunction func : functions) {
                Map<String, Object> json = func.toJSONObject(CapEntityUtil.getColumnName(func), null);
                json.remove("SPLIT");
                funcArray.add(json);
            }
            result.set("functions", funcArray);
        }

        return result;
    }

    /**
     * 編輯資料
     * 
     * @param request
     *            IRequest
     * @return {@link tw.com.iisi.cap.response.Result}
     * @throws CapException
     */
    public Result save(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        String code = request.get("code");
        String isNew = request.get("isNew");
        DefaultFunction function = null;

        if (!CapString.isEmpty(code)) {
            function = functionSetService.findFunctionByCode(code);
            if (isNew.equals("true") && function != null) {
                throw new CapMessageException(CapAppContext.getMessage("js.data.exists"), FunctionSetHandler.class);
            }
        }
        functionSetService.save(function, request);
        return result;
    }

    /**
     * 編輯資料
     * 
     * @param request
     *            IRequest
     * @return {@link tw.com.iisi.cap.response.Result}
     * @throws CapException
     */
    public Result saveRfList(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        String code = request.get("code");
        List<Object> roleItem = GsonUtil.jsonToObjectList(request.get("roleItem"));
        DefaultFunction codeItem = null;

        if (!CapString.isEmpty(code)) {
            codeItem = functionSetService.findFunctionByCode(code);
        }
        if (codeItem == null) {
            throw new CapMessageException(CapAppContext.getMessage("EXCUE_ERROR"), RoleSetHandler.class);
        }

        List<RoleFunction> setRole = new ArrayList<RoleFunction>();
        if (roleItem != null) {
            for (Object item : roleItem) {
                Map<String, Object> role = GsonUtil.objToMap(item);
                RoleFunction rlf = new RoleFunction();
                rlf.setRoleCode((String) role.get("code"));
                rlf.setFuncCode(Integer.toString(codeItem.getCode()));
                rlf.setUpdater(CapSecurityContext.getUserId());
                rlf.setUpdateTime(CapDate.getCurrentTimestamp());
                setRole.add(rlf);
            }
        }
        commonSrv.save(setRole);

        return result;
    }

    /**
     * 刪除資料
     * 
     * @param request
     *            IRequest
     * @return {@link tw.com.iisi.cap.response.Result}
     * @throws CapException
     */
    public Result delete(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        DefaultFunction code = functionSetService.findFunctionByCode(request.get("code"));
        if (code != null) {
            commonSrv.delete(code);
        }
        return result;
    }

    /**
     * 刪除資料
     * 
     * @param request
     *            IRequest
     * @return {@link tw.com.iisi.cap.response.Result}
     * @throws CapException
     */
    public Result deleteRfList(Request request) {
        AjaxFormResult result = new AjaxFormResult();
        String code = request.get("code");
        List<Object> roleItem = GsonUtil.jsonToObjectList(request.get("roleItem"));

        if (CapString.isEmpty(code)) {
            throw new CapMessageException(CapAppContext.getMessage("EXCUE_ERROR"), RoleSetHandler.class);
        }

        List<String> delRole = new ArrayList<String>();
        if (roleItem != null) {
            for (Object item : roleItem) {
                Map<String, Object> role = GsonUtil.objToMap(item);
                delRole.add((String) role.get("code"));
            }
        }
        functionSetService.deleteRfList(code, delRole);

        return result;
    }

}
