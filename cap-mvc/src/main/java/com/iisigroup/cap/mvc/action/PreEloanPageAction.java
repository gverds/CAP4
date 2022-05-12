/* 
 * EloanPageAction.java
 * 
 * Copyright (c) 2019 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.mvc.action;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.component.impl.CapSpringMVCRequest;
import com.iisigroup.cap.security.CapSecurityContext;
import com.iisigroup.cap.security.model.CapUserDetails;
import com.iisigroup.cap.utils.CapAppContext;

/**
 * <pre>
 * ecol page action
 * </pre>
 * 
 * @since 2019年8月22日
 * @author Lancelot
 * @version
 *          <ul>
 *          <li>2019年8月22日,Lancelot,new
 *          </ul>
 */
@Controller
@RequestMapping("/*")
public class PreEloanPageAction extends BaseActionController {

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public ModelAndView error(Locale locale, HttpServletRequest request, HttpServletResponse response) {
        String path = request.getPathInfo();
        ModelAndView model = new ModelAndView(path);
        HttpSession session = request.getSession(false);
        response.setStatus(HttpServletResponse.SC_OK);
        final AuthenticationException ae = (session != null) ? (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) : null;
        String errmsg = "";
        if (ae != null) {
            errmsg = ae.toString();
        } else {
            AccessDeniedException accessDenied = (AccessDeniedException) request.getAttribute(WebAttributes.ACCESS_DENIED_403);
            if (accessDenied != null) {
                errmsg = CapAppContext.getMessage("AccessCheck.AccessDenied", locale) + errmsg;
            }
        }
        model.addObject("errorMessage", errmsg);
        return model;
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public ModelAndView getPage(Locale locale, HttpServletRequest request, HttpServletResponse response) {
        String path = request.getPathInfo();
        ModelAndView model = new ModelAndView(path);
        CapUserDetails userDetails = CapSecurityContext.getUser();
        if (userDetails != null) {
            model.addObject("userDetails", userDetails);
            model.addObject("allowEnter", (userDetails.getRoles() != null && userDetails.getRoles().size() > 0));
        }
        return model;
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    public ModelAndView handleRequestInternal(Locale locale, HttpServletRequest request, HttpServletResponse response) {
        String path = request.getPathInfo();
        ModelAndView model = new ModelAndView(path);
        CapUserDetails userDetails = CapSecurityContext.getUser();
        if (userDetails != null) {
            model.addObject("userDetails", userDetails);
        }
        return model;
    }

    private Map<String, String> getParameterMap(HttpServletRequest request) {
        Request req = getDefaultRequest();
        req.setRequestObject(request);
        Enumeration<String> fids = request.getParameterNames();
        Map<String, String> reqMap = new HashMap<>();
        while (fids.hasMoreElements()) {
            String field = fids.nextElement();
            String value = req.get(field);
            reqMap.put(field, value);
        }
        return reqMap;
    }

    private Request getDefaultRequest() {
        Request cr = CapAppContext.getBean("CapDefaultRequest");
        return cr != null ? cr : new CapSpringMVCRequest();
    }
}
