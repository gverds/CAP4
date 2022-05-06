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
 * LdapAuthenticationToken 供Provider設定驗證哪種類型的AuthenticationToken
 * </pre>
 * 
 * @since 2018年4月12日
 * @author 1607006NB01
 * @version
 *          <ul>
 *          <li>2018年4月12日,Rudy,new
 *          </ul>
 */
public class LdapAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    /**
     * LdapAuthenticationToken建構子
     * 
     * @param principal
     * @param credentials
     */
    public LdapAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    /**
     * LdapAuthenticationToken建構子
     * 
     * @param principal
     * @param credentials
     * @param authorities
     */
    public LdapAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
