/* 
 * CapDefaultUserDetailsService.java
 * 
 * Copyright (c) 2016 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.security.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.iisigroup.cap.security.model.CapUserDetails;
import com.iisigroup.cap.security.model.Role;
import com.iisigroup.cap.security.model.User;

/**
 * <pre>
 * Get default CapUserDetails
 * </pre>
 * 
 * @since 2016年6月14日
 * @author Sunkist Wang
 * @version
 *          <ul>
 *          <li>2016年6月14日,Sunkist Wang,new
 *          </ul>
 */
public class CapDefaultUserDetailsService implements UserDetailsService {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CapUserDetails u = new CapUserDetails(new User() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getCode() {
                return null;
            }

            @Override
            public String getName() {
                return username;
            }

            @Override
            public String getDepCode() {
                return null;
            }

            @Override
            public String getDepName() {
                return null;
            }

            @Override
            public String getStatusDesc() {
                return null;
            }

            @Override
            public String getUpdater() {
                return null;
            }

            @Override
            public Timestamp getUpdateTime() {
                return null;
            }

            @Override
            public List<? extends Role> getRoles() {
                return null;
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getStatus() {
                return null;
            }

            @Override
            public String getEmail() {
                return null;
            }

        }, "P@ssw0rd", new HashMap<>());
        return u;
    }

}
