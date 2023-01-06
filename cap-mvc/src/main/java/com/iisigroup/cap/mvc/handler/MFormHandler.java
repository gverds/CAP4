/*_
 * Copyright (c) 2009-2011 International Integrated System, Inc. 
 * 11F, No.133, Sec.4, Minsheng E. Rd., Taipei, 10574, Taiwan, R.O.C.
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */

package com.iisigroup.cap.mvc.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.util.StringUtils;

import com.iisigroup.cap.action.Action;
import com.iisigroup.cap.annotation.CheckFlow;
import com.iisigroup.cap.annotation.HandlerType;
import com.iisigroup.cap.annotation.HandlerType.HandlerTypeEnum;
import com.iisigroup.cap.component.GridResult;
import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.Result;
import com.iisigroup.cap.component.impl.AjaxFormResult;
import com.iisigroup.cap.constants.GridEnum;
import com.iisigroup.cap.context.CapParameter;
import com.iisigroup.cap.db.dao.CommonDao;
import com.iisigroup.cap.db.dao.SearchSetting;
import com.iisigroup.cap.db.dao.impl.SearchSettingImpl;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.exception.CapFileDownloadException;
import com.iisigroup.cap.exception.CapFlowException;
import com.iisigroup.cap.exception.CapMessageException;
import com.iisigroup.cap.model.GenericBean;
import com.iisigroup.cap.model.OpStepContext;
import com.iisigroup.cap.operation.Operation;
import com.iisigroup.cap.operation.OperationStep;
import com.iisigroup.cap.plugin.HandlerPlugin;
import com.iisigroup.cap.utils.CapAppContext;
import com.iisigroup.cap.utils.CapBeanUtil;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.GsonUtil;

/**
 * <pre>
 * 直接以method name來執行(formAction傳入method name)
 * 若未指定method時，預設執行doWork()
 * </pre>
 * 
 * @since 2010/12/8
 * @author iristu
 * @version
 *          <ul>
 *          <li>2010/12/8,iristu,new
 *          <li>2011/11/1,rodeschen,from cap marge from and grid
 *          <li>2012/3/8,rodeschen,add column marge index and name
 *          <li>2012/9/20,iristu,改由HandlerType來判斷取得Operation
 *          </ul>
 */
public abstract class MFormHandler extends HandlerPlugin {

    @Resource(name = "handlerOpMapping")
    private CapParameter handlerOp;

    @Resource
    private HttpSession session;

    @Resource
    private CommonDao commonDao;

    /**
     * <pre>
     * 直接以method name來執行
     * </pre>
     * 
     * @param formAction
     *            action
     * @return IAction
     */
    @Override
    public Action getAction(String formAction) {
        return new MethodExecuteAction(this);
    }

    /**
     * <pre>
     * MethodExecuteAction
     * </pre>
     */
    private class MethodExecuteAction implements Action {

        MFormHandler executeHandler;

        public MethodExecuteAction(MFormHandler executeObj) {
            this.executeHandler = executeObj;
        }

        @Override
        public Result doWork(Request params) {
            Result rtn = null;
            String methodId = params.get(FORM_ACTION);
            if (CapString.isEmpty(methodId)) {
                methodId = "doWork";
            }
            boolean hasMethod = false;
            try {
                Method method = CapBeanUtil.findMethod(executeHandler.getClass(), methodId, (Class<?>) null);
                if (method != null) {
                    HandlerType type = method.getAnnotation(HandlerType.class);
                    if (type != null && HandlerTypeEnum.GRID.equals(type.value())) {
                        rtn = getGridData(method, params);
                    } else {
                        CheckFlow checkFlow = method.getAnnotation(CheckFlow.class);
                        if (checkFlow != null && !StringUtils.isEmpty(checkFlow.name())) {
                            Object flowDao = CapAppContext.getApplicationContext().getBean("lmcmFlowmstrDaoImpl");
                            Method flowMethod = CapBeanUtil.findMethod(flowDao.getClass(), "findByMid", (Class<?>) null);
                            if (flowMethod != null) {
                                GenericBean flowmstr = (GenericBean) flowMethod.invoke(flowDao, checkFlow.name());
                                if (flowmstr != null) {
                                    if (!((Integer) flowmstr.get("flowSched")).equals(Integer.parseInt(params.get("flowSched")))) {
                                        throw new CapFileDownloadException("流程節點已異動，請重新開啟案件。");
                                    }
                                }
                            }
                        }
                        rtn = (Result) method.invoke(executeHandler, params);
                    }
                    hasMethod = true;
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof CapMessageException) {
                    throw (CapMessageException) e.getCause();
                } else if (e.getCause() instanceof CapException) {
                    throw (CapException) e.getCause();
                } else {
                    throw new CapException(e.getCause(), executeHandler.getClass());
                }
            } catch (CapFlowException e) {
                throw new CapFlowException(e.getMessage());
            } catch (Throwable t) {
                throw new CapException(t, executeHandler.getClass());
            } finally {
                // handler method 結束後，強制進行 flush
                try {
                    commonDao.clear();
                } catch (Exception e) {
                    // do nothing
                    // 不一定每個 handler method 都會 bind SharedEntityManager
                    logger.warn("clear persistenceContext fail.", e);
                }
            }
            if (!hasMethod) {
                throw new CapMessageException("action not found", getClass());
            }
            return rtn;
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Result getGridData(Method method, Request params) {
        SearchSetting search = createSearchTemplete();
        boolean pages = params.containsParamsKey(GridEnum.PAGE.getCode());
        int page = 0, pageRows = 0, startRow = 0;
        if (pages) {
            // page = params.getParamsAsInteger(GridEnum.PAGE.getCode());
            pageRows = params.getParamsAsInteger(GridEnum.PAGEROWS.getCode());
            // startRow = (page - 1) * pageRows;
            startRow = params.getParamsAsInteger(GridEnum.START.getCode());
            page = startRow / pageRows + 1;
            search.setFirstResult(startRow).setMaxResults(pageRows);
        }
        boolean sort = params.containsParamsKey(GridEnum.SORTCOLUMN.getCode()) && !CapString.isEmpty(params.get(GridEnum.SORTCOLUMN.getCode()));
        if (sort) {
            String[] sortBy = params.get(GridEnum.SORTCOLUMN.getCode()).split("\\|");
            String[] isAscAry = params.get(GridEnum.SORTTYPE.getCode(), "asc").split("\\|");
            for (int i = 0; i < sortBy.length; i++) {
                String isAsc = (i < isAscAry.length) ? isAscAry[i] : "asc";
                search.addOrderBy(sortBy[i], !GridEnum.SORTASC.getCode().equals(isAsc));
            }
        }
        GridResult result = null;
        AjaxFormResult wrapper = new AjaxFormResult();
        try {
            result = (GridResult) method.invoke(this, search, params);
            result.setColumns(getColumns(params.get(GridEnum.COL_PARAM.getCode(), false)));
            result.setPage(page);
            // result.setPageCount(result.getRecords(), pageRows);
            wrapper.set("recordsTotal", result.getRecords());
            wrapper.set("recordsFiltered", result.getRecords());
            wrapper.set("data", result.getRowDataToList());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CapMessageException) {
                throw (CapMessageException) e.getCause();
            } else if (e.getCause() instanceof CapException) {
                throw (CapException) e.getCause();
            } else {
                throw new CapException(e.getCause(), this.getClass());
            }
        } catch (Throwable t) {
            throw new CapException(t, this.getClass());
        }
        return wrapper;
    }

    /**
     * 取得iGrid中的Column Name
     * 
     * @param params
     *            String
     * @return String string[]
     */
    @SuppressWarnings("unchecked")
    protected String[] getColumns(String params) {
        List<Object> arr = GsonUtil.jsonToObjectList(params);
        String[] colNames = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            Map<String, String> m = (Map<String, String>) GsonUtil.objToObj(arr.get(i));
            if (m.containsKey(GridEnum.COL_INDEX.getCode())) {
                colNames[i] = new StringBuffer().append(m.get(GridEnum.COL_NAME.getCode())).append("|").append(m.get(GridEnum.COL_INDEX.getCode())).toString();
            } else {
                colNames[i] = m.get(GridEnum.COL_NAME.getCode());
            }
        }
        return colNames;
    };

    /**
     * <pre>
     * 若未傳送formAction值，則default執行此method
     * </pre>
     * 
     * @param params
     *            PageParameters
     * @return IResult
     */
    public Result doWork(Request params) {
        return null;
    }

    @Override
    public Result execute(Request params) {
        Operation oper = getOperation(params);
        if (oper != null) {
            OpStepContext ctx = new OpStepContext(OperationStep.NEXT);
            oper.execute(ctx, params, this);
            return ctx.getResult();
        }
        return null;
    }

    protected String getOperationName(Request params) {
        String methodId = params.get(FORM_ACTION);
        Method method = CapBeanUtil.findMethod(this.getClass(), methodId, (Class<?>) null);
        if (method != null) {
            HandlerType type = method.getAnnotation(HandlerType.class);
            if (type != null) {
                String op = type.name();
                if (op == null || "".equals(op)) {
                    op = type.value().name();
                }
                return handlerOp.getValue(op, SIMPLE_OPERATION);
            }
        }
        return SIMPLE_OPERATION;
    }

    protected Operation getOperation(Request params) {
        return (Operation) CapAppContext.getApplicationContext().getBean(getOperationName(params));
    }

    private SearchSetting createSearchTemplete() {
        return new GridSearch();
    }

    /**
     * <pre>
     * GridSearch extends AbstractSearchSetting
     * </pre>
     */
    private class GridSearch extends SearchSettingImpl {

        private static final long serialVersionUID = 1L;

    }

    @Override
    public String getHandlerName() {
        return getPluginName();
    }

}// ~
