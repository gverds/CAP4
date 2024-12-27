/* 
 * LdapAndDbAuthenticationProvider.java
 * 
 * Copyright (c) 2018 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.mvc.provider;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.iisigroup.cap.exception.CapMessageException;
import com.iisigroup.cap.mvc.auth.exception.CapAuthenticationException;
import com.iisigroup.cap.mvc.model.DefUser;
import com.iisigroup.cap.mvc.token.LdapAuthenticationToken;
import com.iisigroup.cap.security.model.CapUserDetails;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.CapSystemConfig;

/**
 * <pre>
 * Spring security provider for LDAP
 * 使用登入的帳號密碼作為管理者帳號密碼登入驗證
 * </pre>
 * 
 * @since 2018年4月20日
 * @author 1607006NB01
 * @version
 *          <ul>
 *          <li>2018年4月11日,Rudy,new
 *          <li>2021/04/15,Tim,change CapAuthenticationException to CapMessageException(for DEV test)
 *          </ul>
 */
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider {

    @Resource
    private CapSystemConfig config;

    private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // Error codes
    private static final int USERNAME_NOT_FOUND = 0x525;
    private static final int INVALID_PASSWORD = 0x52e;
    private static final int NOT_PERMITTED = 0x530;
    private static final int PASSWORD_EXPIRED = 0x532;
    private static final int ACCOUNT_DISABLED = 0x533;
    private static final int ACCOUNT_EXPIRED = 0x701;
    private static final int PASSWORD_NEEDS_RESET = 0x773;
    private static final int ACCOUNT_LOCKED = 0x775;

    // TODOed after ldap test done, change to final string.
    /**
     * (測試)tcbt.com (10.0.6.1, 10.0.6.2) (正式)tcb.com (10.0.31.1, 10.0.31.2)但實際ping到的是10.0.31.2
     */
    private String domain;
    private String rootDn;
    private String url;
    private String urlSSL;
    private String qryAcct;
    private String qryXxd;
    private String qryDn;
    private String searchFilter = "(&(objectClass=user)(userPrincipalName={0}))";
    // TODOed after ladp test done, you can close debugger flag
    private boolean isDebugger = true;
    /**
     * is use self SSL certificate control(trust all certs)
     */
    private boolean isSSL = false;
    /**
     * is use self SSL certificate control(trust all certs)
     */
    private String enableSSL = "false";
    /**
     * cache LDAP Ctx search result(change to attributes)
     */
    private Map<String, Object> CtxAttrs;

    // Only used to allow tests to substitute a mock LdapContext
    ContextFactory contextFactory = new ContextFactory();

    /**
     * @param domain
     *            the domain name (may be null or empty)
     * @param url
     *            an LDAP url (or multiple URLs)
     * @param rootDn
     *            the root DN (may be null or empty)
     */
    public LdapAuthenticationProvider(String domain, String url, String urlSSL, String rootDn, String qryAcct, String qryXxd, String qryDn) {
        Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
        this.setDomain(StringUtils.hasText(domain) ? domain : null);
        this.setUrl(url);
        this.setUrlSSL(urlSSL);
        this.setRootDn(StringUtils.hasText(rootDn) ? rootDn : null);
        this.setQryAcct(StringUtils.hasText(qryAcct) ? qryAcct : null);
        this.setQryXxd(StringUtils.hasText(qryXxd) ? qryXxd : null);
        this.setQryDn(StringUtils.hasText(qryDn) ? qryDn : null);
    }

    /**
     * @param domain
     *            the domain name (may be null or empty)
     * @param url
     *            an LDAP url (or multiple URLs)
     */
    public LdapAuthenticationProvider(String domain, String url) {
        Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
        this.setDomain(StringUtils.hasText(domain) ? domain.toLowerCase() : null);
        this.setUrl(url);
        setRootDn(this.getDomain() == null ? null : rootDnFromDomain(this.getDomain()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider#doAuthentication(org.springframework.security.authentication.UsernamePasswordAuthenticationToken)
     */
    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        String password = (String) auth.getCredentials();
        // LdapContext lctx
        DirContext ctx = bindAsUser(username, password, getIsSSL());
        try {
            return searchForUser(ctx, username);
        } catch (NamingException e) {
            throw new CapMessageException("Failed to locate directory entry for authenticated user: " + username, e, this.getClass());
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    public List<String[]> queryDepartmentUserList(String department) {
        String username = config.getProperty("qryAcct");
        String password = config.getProperty("qryXxd");
        logger.debug("department = [" + department + "]");
        // LdapContext lctx
        DirContext ctx = bindAsUser(username, password, getIsSSL());
        try {
            // String depDn = ",OU=IISI,DC=iead,DC=local";
            // TCB test
            // String searchRoot = "OU={DEP},OU=TCBUsers,DC=tcbt,DC=com";
            String depDn = this.qryDn == null ? config.getProperty("qryDn") : this.qryDn;
            department = "OU=" + department;
            // depDn={DEP},OU=TCBUsers,DC=tcbt,DC=com
            String depNumber = depDn.replace("{DEP}", department);
            return listDepartmentUsers(ctx, username, depNumber);
        } catch (NamingException e) {
            throw new CapMessageException("Failed to locate directory entry for authenticated user: " + username, e, this.getClass());
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    public List<String[]> queryDepartmentUserList(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        String password = (String) auth.getCredentials();
        // LdapContext lctx
        DirContext ctx = bindAsUser(username, password, getIsSSL());
        try {
            String depNumber = queryDepartment(ctx, username);
            return listDepartmentUsers(ctx, username, depNumber);
        } catch (NamingException e) {
            throw new CapMessageException("Failed to locate directory entry for authenticated user: " + username, e, this.getClass());
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    private SearchControls getSimpleSearchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setTimeLimit(10000);
        // for test
        // String[] attrIDs = {"objectGUID"};
        // searchControls.setReturningAttributes(attrIDs);
        // searchControls.setSearchScope(SearchControls.OBJECT_SCOPE );
        return searchControls;
    }

    /**
     * 取得使用者權限 Creates the user authority list from the values of the {@code memberOf} attribute obtained from the user's Active Directory entry.
     * 
     * @param userData
     * @param username
     * @param password
     * @return Collection
     */
    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {

        CapUserDetails capUser = getUserWithDefaultRole(username, password);
        // TODO remove under block after test ldap done.
        Attributes attributes = userData.getAttributes();
        NamingEnumeration<?> namingEnum = attributes.getIDs();

        Map<String, Object> map = this.getCtxAttrs();
        String deptId = (String) map.get("deptId"); // 債管登入單位
        String ouDeptId = "";
        Map<String, Object> extraAttr = new HashMap<String, Object>();
        // 清空前一次資料
        this.setCtxAttrs(null);
        try {
            logger.debug("**********************");
            while (namingEnum.hasMore()) {
                Object objId = namingEnum.next();
                if (objId instanceof String) {
                    Attribute attr = attributes.get((String) objId);
                    String id = (String) objId;
                    logger.debug(id + " : {}", attr);
                    extraAttr.put(id, attr.toString());
                    try {
                        // Ldap Context查詢回來的結果，哪一個namingEnum欄位是locale???
                        if ("countryCode".equals(attr.getID()) && attr.size() > 0) {
                            if (attr.get(0) != null) {
                                logger.debug("countryCode : {}", attr.get(0));
                                capUser.setLocale(LocaleUtils.toLocale(attr.get(0).toString()));
                            }
                        }
                        if ("ou".equals(attr.getID())) {
                            // AD deptId，確認是否與債管登入單位相同
                            ouDeptId = (String) attr.get(0);
                        }
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                }
            }
            logger.debug("**********************");
            namingEnum.close();
            this.setCtxAttrs(extraAttr);
        } catch (NamingException e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
        }
        // TODO remove upper block after test ldap done.
        // 確認 登入者AD單位與債管系統單位 是否一致
        if (!CapString.isEmpty(deptId) && !deptId.equals(ouDeptId)) {
            throw new BadCredentialsException("601");
        }
        return capUser.getAuthorities();

    }

    /**
     * LDAP驗證通過後 回傳含有CapUserDetails的Authentication
     * 
     * @param authentication
     * @param user
     * @return Authentication
     */
    @Override
    protected Authentication createSuccessfulAuthentication(UsernamePasswordAuthenticationToken authentication, UserDetails user) {
        CapUserDetails capUser = getUserWithDefaultRole(user.getUsername(), user.getPassword());
        if (capUser.getAuthorities() != null) {
            try { // TODO remove after test ldap done.
                for (GrantedAuthority sga : capUser.getAuthorities()) {
                    if (sga instanceof SimpleGrantedAuthority) {
                        logger.debug("SIT LDAP >> authorities : {}", ((SimpleGrantedAuthority) sga).getAuthority());
                    }
                }
            } catch (Exception e) {
                logger.error("LDAP error >> " + e.getLocalizedMessage(), e);
            }
        }
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(capUser, user.getPassword(), user.getAuthorities());
        return result;
    }

    /**
     * 設定Provider驗證哪種類型的AuthenticationToken
     * 
     * @param authentication
     *            傳入AuthenticationToken
     * @return boolean
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return LdapAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 提供部分呼叫，並且使用LDAP（非SSL LDAP）
     * 
     * @param username
     * @param password
     * @return
     */
    public DirContext bindAsUser(String username, String password) {
        return bindAsUser(username, password, false);
    }

    /**
     * For getRandomNumberInRange(5, 10), this will generates a random integer between 5 (inclusive) and 10 (inclusive).
     * 
     * @param min
     * @param max
     * @return
     */
    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        // 弱掃修復
        SecureRandom rand = new SecureRandom();
        rand.setSeed((new Date()).getTime());
        return rand.nextInt((max - min) + 1) + min;
    }

    private DirContext bindAsUser(String username, String password, boolean isSSL) {
        // TODOed add DNS lookup based on domain
        // final String bindUrl = getUrl();
        String bindUrl = "";
        bindUrl = getUrl();
        if (CapString.isEmpty(bindUrl)) {
            bindUrl = config.getProperty("ldapUrl");
        }
        if (isSSL) {
            if (!CapString.isEmpty(getUrlSSL())) {
                bindUrl = getUrlSSL();
            } else {
                bindUrl = config.getProperty("ldapUrlSSL");
            }
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        String bindPrincipal = createBindPrincipal(username);
        // for TCB test bindPrincipal : ELOANAD@tcbt.com
        env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
        env.put(Context.PROVIDER_URL, bindUrl);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.read.timeout", "5000");
        // Specify timeout to be 5 seconds
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());

        if (isSSL) {
            try {
                env.put("java.naming.ldap.factory.socket", "com.iisigroup.cap.mvc.auth.service.UnsecuredSSLSocketFactory");
                // 2021/04/21,Tim,for No subject alternative DNS name matching (ldap server ip access) found
                System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
            } catch (Exception e) {
                logger.error("LDAPS create sslsocketfactory fail", e);
            }
        } else {
            System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "false");
        }
        try {
            return contextFactory.createContext(env);
            // } catch (NamingException namingException) {
            // logger.error("LDAP auth fail >> "+bindPrincipal, namingException);
            // if ((namingException instanceof AuthenticationException) || (namingException instanceof OperationNotSupportedException)) {
            // handleBindException(bindPrincipal, namingException);
            // throw new CapMessageException("Failed to locate directory entry for authenticated user: " + username, namingException, this.getClass());
            // } else {
            // throw new CapMessageException(LdapUtils.convertLdapException(namingException).getMessage(), namingException, this.getClass());
            // }
        } catch (Exception namingException) {

            logger.error("LDAP auth fail >> " + bindPrincipal, namingException);
            if ((namingException instanceof AuthenticationException) || (namingException instanceof OperationNotSupportedException)) {
                handleBindException(bindPrincipal, (NamingException) namingException);
                logger.error("Failed to locate directory entry for authenticated user: " + username, namingException, this.getClass());
                // throw new CapMessageException("Failed to locate directory entry for authenticated user: " + username, namingException, this.getClass());
            } else {
                logger.error(LdapUtils.convertLdapException((NamingException) namingException).getMessage(), namingException, this.getClass());
                // throw new CapMessageException(LdapUtils.convertLdapException((NamingException) namingException).getMessage(), namingException, this.getClass());
            }
            // throw new CapMessageException("Connection timed out", connExcetion, this.getClass());
            // 2022/6/14,連不到AD server1,嘗試連另一組url
            try {
                int i = getRandomNumberInRange(2, 4);
                if (isSSL) {
                    bindUrl = config.getProperty("ldap" + i + "UrlSSL");
                } else {
                    bindUrl = config.getProperty("ldap" + i + "Url");
                }
                env.clear();
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                bindPrincipal = createBindPrincipal(username);
                // for TCB test bindPrincipal : ELOANAD@tcbt.com
                env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
                env.put(Context.PROVIDER_URL, bindUrl);
                env.put(Context.SECURITY_CREDENTIALS, password);
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put("com.sun.jndi.ldap.read.timeout", "5000");
                // Specify timeout to be 5 seconds
                env.put("com.sun.jndi.ldap.connect.timeout", "5000");
                env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());
                if (isSSL) {
                    try {
                        env.put("java.naming.ldap.factory.socket", "com.iisigroup.cap.mvc.auth.service.UnsecuredSSLSocketFactory");
                        // 2021/04/21,Tim,for No subject alternative DNS name matching (ldap server ip access) found
                        System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
                    } catch (Exception e) {
                        logger.error("LDAPS create sslsocketfactory fail", e);
                    }
                } else {
                    System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "false");
                }
                return contextFactory.createContext(env);
            } catch (Exception e) {
                throw new CapMessageException("Connection fail, try 2nd ldapUrl is [" + bindUrl + "]", e, this.getClass());
            }
        }
    }

    private void handleBindException(String bindPrincipal, NamingException exception) {
        if (logger.isDebugEnabled() || isDebugger) {
            logger.debug("Authentication for " + bindPrincipal + " failed:" + exception);
        }

        int subErrorCode = parseSubErrorCode(exception.getMessage());

        if (subErrorCode <= 0) {
            logger.debug("Failed to locate AD-specific sub-error code in message");
            return;
        }

        logger.info("Active Directory authentication failed: " + subCodeToLogMessage(subErrorCode));

    }

    private int parseSubErrorCode(String message) {
        Matcher m = SUB_ERROR_CODE.matcher(message);

        if (m.matches()) {
            return Integer.parseInt(m.group(1), 16);
        }

        return -1;
    }

    private String subCodeToLogMessage(int code) {
        switch (code) {
        case USERNAME_NOT_FOUND:
            return "User was not found in directory";
        case INVALID_PASSWORD:
            return "Supplied password was invalid";
        case NOT_PERMITTED:
            return "User not permitted to logon at this time";
        case PASSWORD_EXPIRED:
            return "Password has expired";
        case ACCOUNT_DISABLED:
            return "Account is disabled";
        case ACCOUNT_EXPIRED:
            return "Account expired";
        case PASSWORD_NEEDS_RESET:
            return "User must reset password";
        case ACCOUNT_LOCKED:
            return "Account locked";
        }

        return "Unknown (error code " + Integer.toHexString(code) + ")";
    }

    /**
     * LDAP查詢user
     * 
     * @param context
     * @param username
     * @return DirContextOperations
     * @throws NamingException
     */
    private DirContextOperations searchForUser(DirContext context, String username) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String bindPrincipal = createBindPrincipal(username);
        String searchRoot = getRootDn() != null ? getRootDn() : searchRootFromPrincipal(bindPrincipal);
        logger.debug("LDAP searchForUser >> principal : {}", bindPrincipal);
        logger.debug("LDAP searchForUser >> searchRoot : {}", searchRoot);
        try {
            // 2021/04/20,Tim,要查詢某分行所有使用者清單故改由參數決定searchfilter條件
            if (CapString.isEmpty(searchFilter)) {
                // 如果只用objectclass=user作為searchFilter,會查到多筆result..發生incorrectResults Exception
                searchFilter = "(&(objectclass=user)(userPrincipalName={0}))";
            }
            // 登入頁面,沒帶查詢條件,有可能變成(&(objectCategory=Person))查資料
            if (searchFilter.indexOf("userPrincipalName") == -1) {
                searchFilter = "(&(objectclass=user)(userPrincipalName={0}))";
            }
            logger.debug("LDAP searchForUser >> searchFilter : {}", searchFilter);
            // 驗證登入者
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(context, searchControls, searchRoot, searchFilter, new Object[] { bindPrincipal });
        } catch (IncorrectResultSizeDataAccessException incorrectResults) {
            // Search should never return multiple results if properly configured - just
            // rethrow
            logger.error("Exception>>" + incorrectResults.getLocalizedMessage(), incorrectResults);
            incorrectResults.printStackTrace();
            if (incorrectResults.getActualSize() != 0) {
                throw new CapMessageException("Incorrect result size: expected " + incorrectResults.getActualSize(), incorrectResults, this.getClass());
            }
            // If we found no results, then the username/password did not match
            throw new CapMessageException("User " + username + " not found in directory.", incorrectResults, this.getClass());
        }
    }

    private String queryDepartment(DirContext context, String username) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String bindPrincipal = createBindPrincipal(username);
        // TCB test
        // String searchRoot = "OU={DEP},OU=TCBUsers,DC=tcbt,DC=com";
        // LOCAL test
        String searchRoot = getRootDn() != null ? getRootDn() : searchRootFromPrincipal(bindPrincipal);
        logger.debug("LDAP searchForUser >> principal : {}", bindPrincipal);
        logger.debug("LDAP searchForUser >> searchRoot : {}", searchRoot);
        searchControls.setReturningAttributes(new String[] { "distinguishedName", "departmentNumber", "department" });
        searchFilter = "(&(objectclass=user)(objectcategory=user)(sAMAccountName={0}))";
        searchFilter = searchFilter.replace("{0}", username);
        // searchRoot = "OU=FBD22,OU=FBD00,OU=IISI,DC=iead,DC=local";
        NamingEnumeration<SearchResult> en = context.search(searchRoot, searchFilter, searchControls);
        String departmentNumber = "";
        if (en == null) {
            System.out.println("Have no NamingEnumeration.");
        }
        if (!en.hasMoreElements()) {
            System.out.println("Have no element.");
        } else {
            // 輸出查到的資料
            while (en.hasMoreElements()) {
                SearchResult result = en.next();
                NamingEnumeration<? extends Attribute> attrs = result.getAttributes().getAll();
                while (attrs.hasMore()) {
                    Attribute attr = attrs.next();
                    if ("distinguishedName".equals(attr.getID())) {
                        String[] manArr = attr.get().toString().split(",");
                        if (manArr.length > 0) {
                            if (manArr.length > 1) {
                                String[] modifiedArray = Arrays.copyOfRange(manArr, 1, manArr.length);
                                for (String s : modifiedArray) {
                                    departmentNumber += s + ",";
                                }
                                departmentNumber = departmentNumber.substring(0, departmentNumber.lastIndexOf(","));
                            } else {
                                departmentNumber = manArr[0];
                            }
                        }
                    }
                    System.out.println("attr.[" + attr.getID() + "]=[" + attr.get() + "]");
                }
            }
        }
        return departmentNumber;
    }

    private List<String[]> listDepartmentUsers(DirContext context, String username, String departmentNumber) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String bindPrincipal = createBindPrincipal(username);
        // LOCAL test
        String searchRoot = "{DEP}";
        logger.debug("LDAP searchForUser >> principal : {}", bindPrincipal);
        logger.debug("LDAP searchForUser >> searchRoot : {}", searchRoot);
        if (!CapString.isEmpty(departmentNumber)) {
            searchRoot = searchRoot.replace("{DEP}", departmentNumber);
        }
        logger.debug("LDAP searchForUser >> searchDepartRoot : {}", searchRoot);
        List<String[]> result = new ArrayList<String[]>();
        try {
            // 2021/04/20,Tim,要查詢某分行所有使用者清單故改由參數決定searchfilter條件
            // 如果只用objectclass=user作為searchFilter,會查到多筆result
            // searchFilter = "(&(objectclass=user))";
            searchFilter = "(&(objectCategory=Person))";

            logger.debug("LDAP searchForUser >> searchFilter : {}", searchFilter);
            // query all department user
            searchControls.setReturningAttributes(new String[] { "cn", "sAMAccountName", "displayName", "telephoneNumber", "extensionAttribute1", "ROCID" });
            try {
                NamingEnumeration<?> answer = context.search(searchRoot, searchFilter, searchControls);
                // Object obj = context.lookup(this.rootDn);
                logger.debug("DepartUser>> Out while loop");
                while (answer.hasMore()) {
                    logger.debug("DepartUser>> while looping");
                    SearchResult rslt = (SearchResult) answer.next();
                    Attributes attrs = rslt.getAttributes();
                    String[] ss = new String[6];
                    ss[0] = attrs.get("cn") != null ? attrs.get("cn").toString().replace("cn:", "").trim().toUpperCase() : "";
                    ss[1] = attrs.get("sAMAccountName") != null ? attrs.get("sAMAccountName").toString().replace("sAMAccountName:", "").trim().toUpperCase() : "";
                    ss[2] = attrs.get("displayName") != null ? attrs.get("displayName").toString().replace("displayName:", "").trim() : "";
                    ss[3] = attrs.get("telephoneNumber") != null ? attrs.get("telephoneNumber").toString().replace("telephoneNumber:", "").trim() : "";
                    ss[4] = attrs.get("extensionAttribute1") != null ? attrs.get("extensionAttribute1").toString().replace("extensionAttribute1:", "").trim() : "";
                    ss[5] = attrs.get("ROCID") != null ? attrs.get("ROCID").toString() : "";
                    if (!ss[5].equals("")) {
                        result.add(ss);
                    }
                    logger.debug("DepartUser>>" + ss[0] + "/" + ss[1] + "/" + ss[2] + "/" + ss[3] + "/" + ss[4]);
                    logger.debug("DepartUser>> while nexttt");
                    context.close();
                }
            } catch (Exception e) {
                logger.error("listDepartmentUsers ERROR: " + e.getLocalizedMessage(), e);
            }

        } catch (IncorrectResultSizeDataAccessException incorrectResults) {
            // Search should never return multiple results if properly configured - just
            // rethrow
            logger.error("Exception>>" + incorrectResults.getLocalizedMessage(), incorrectResults);
            incorrectResults.printStackTrace();
            if (incorrectResults.getActualSize() != 0) {
                throw new CapMessageException("Incorrect result size: expected " + incorrectResults.getActualSize(), incorrectResults, this.getClass());
            }
            // If we found no results, then the username/password did not match
            throw new CapMessageException("User " + username + " not found in directory.", incorrectResults, this.getClass());
        }

        return result;
    }

    private String searchRootFromPrincipal(String bindPrincipal) {
        int atChar = bindPrincipal.lastIndexOf('@');

        if (atChar < 0) {
            throw new CapAuthenticationException("User principal '" + bindPrincipal + "' does not contain the domain, and no domain has been configured");
        }

        return rootDnFromDomain(bindPrincipal.substring(atChar + 1, bindPrincipal.length()));
    }

    // TCB test domain 待確認：tcbt.com
    private String rootDnFromDomain(String domain) {
        String[] tokens = StringUtils.tokenizeToStringArray(domain, ".");
        StringBuilder root = new StringBuilder();

        for (String token : tokens) {
            if (root.length() > 0) {
                root.append(',');
            }
            root.append("dc=").append(token);
        }
        logger.debug("rootDn value::{}", root.toString());
        return root.toString();
    }

    String createBindPrincipal(String username) {
        if (username.toUpperCase(Locale.ENGLISH).equals("SCRIPT")) {
            // 弱掃修復
            return null;
        }
        if (getDomain() == null || username.toLowerCase(Locale.ENGLISH).endsWith(getDomain())) {
            return username;
        }
        // TODO "domainName\\administrator"; //注意用戶名的寫法：domain\User 或// User@domain.com
        return username + "@" + getDomain();
    }

    /**
     * The LDAP filter string to search for the user being authenticated. Occurrences of {0} are replaced with the {@code username@domain}.
     * <p>
     * Defaults to: {@code (&(objectClass=user)(userPrincipalName= 0}))}
     * </p>
     *
     * @param searchFilter
     *            the filter string
     *
     * @since 3.2.6
     */
    public void setSearchFilter(String searchFilter) {
        Assert.hasText(searchFilter, "searchFilter must have text");
        this.searchFilter = searchFilter;
    }

    static class ContextFactory {
        DirContext createContext(Hashtable<?, ?> env) throws NamingException {
            return new InitialLdapContext(env, null);
        }
    }

    /**
     * 建立預設的CapUserDetails 角色為AI0001
     * 
     * @param username
     * @param password
     * @return CapUserDetails
     */
    private CapUserDetails getUserWithDefaultRole(String username, String password) {
        Map<String, String> roles = new HashMap<String, String>();
        roles.put("AI1", "test");
        // 做假USER 要用建構子才set得進去Authorities()
        CapUserDetails capUser = new CapUserDetails(new DefUser(), password, roles);
        capUser.setUserId(username);

        return capUser;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRootDn() {
        return rootDn;
    }

    public void setRootDn(String rootDn) {
        this.rootDn = rootDn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getIsSSL() {
        this.isSSL = "true".equals(this.enableSSL);
        return this.isSSL;
    }

    public String getEnableSSL() {
        return this.enableSSL;
    }

    public void setEnableSSL(String enableSSL) {
        this.enableSSL = enableSSL;
        this.isSSL = "true".equals(this.enableSSL);
    }

    public Map<String, Object> getCtxAttrs() {
        return CtxAttrs;
    }

    public void setCtxAttrs(Map<String, Object> ctxAttrs) {
        CtxAttrs = ctxAttrs;
    }

    /**
     * @return the 查詢部門ListAcct
     */
    public String getQryAcct() {
        return qryAcct;
    }

    /**
     * @param qryAcct
     *            the 查詢部門ListAcct to set
     */
    public void setQryAcct(String qryAcct) {
        this.qryAcct = qryAcct;
    }

    /**
     * @return the qryXxd
     */
    public String getQryXxd() {
        return qryXxd;
    }

    /**
     * @param qryXxd
     *            the qryXxd to set
     */
    public void setQryXxd(String qryXxd) {
        this.qryXxd = qryXxd;
    }

    /**
     * @return the qryDn 查詢部門List條件({DEP},OU=TCBUsers,DC=tcb,DC=com)
     */
    public String getQryDn() {
        return qryDn;
    }

    /**
     * @param qryDn
     *            the 查詢部門List條件({DEP},OU=TCBUsers,DC=tcb,DC=com) to set
     */
    public void setQryDn(String qryDn) {
        this.qryDn = qryDn;
    }

    /**
     * @return the 加密AD server url
     */
    public String getUrlSSL() {
        return urlSSL;
    }

    /**
     * @param urlSSL
     *            the 加密AD server url to set
     */
    public void setUrlSSL(String urlSSL) {
        this.urlSSL = urlSSL;
    }
}
