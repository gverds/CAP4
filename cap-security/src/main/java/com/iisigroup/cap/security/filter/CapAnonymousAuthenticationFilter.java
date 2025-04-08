package com.iisigroup.cap.security.filter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import com.iisigroup.cap.security.model.CapUserDetails;

/**
 * <pre>
 * TODO Write a short description on the purpose of the program
 * </pre>
 * 
 * @since 2020年9月11日
 * @author 1104263
 * @version
 *          <ul>
 *          <li>2020年9月11日,1104263,new
 *          <li>2021/9/8,#18899,HTML5: Missing Content Security Policy
 *          </ul>
 */
public class CapAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {
    /**
     * @param key
     */
    public CapAnonymousAuthenticationFilter(String key) {
        super(key, getDefautlUserDetails(), AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }
    private static CapUserDetails getDefautlUserDetails() {
        CapUserDetails user = new CapUserDetails();
        user.setUserId("SYS");
        user.setUserName("SYS");
        user.setUnitNo("999");
        user.setUnitName("SYSTEM");
        Map<String, String> defaultMap = new HashMap<String, String>();
        defaultMap.put("SYS", "SYS");
        user.setRoles(defaultMap);
        return user;
    }
}
