/* 
 * LdapAuthenticationToken.java
 * 
 * Copyright (c) 2018 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.mvc.token;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * <pre>
 * CapAuthenticationToken 供Provider設定驗證哪種類型的AuthenticationToken
 * </pre>
 * 
 * @since 2018年4月12日
 * @author 1607006NB01
 * @version
 *          <ul>
 *          <li>2018年4月12日,Rudy,new
 *          </ul>
 */
public class CapAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    /**
     * CapAuthenticationToken建構子
     * 
     * @param principal
     * @param credentials
     */
    public CapAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    /**
     * CapAuthenticationToken建構子
     * 
     * @param principal
     * @param credentials
     * @param authorities
     */
    public CapAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
