package com.iisigroup.cap.mvc.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.iisigroup.cap.mvc.token.CapAuthenticationToken;
import com.iisigroup.cap.mvc.token.LdapAuthenticationToken;

/**
 * <pre>
 * 繼承UsernamePasswordAuthenticationFilter
 * 根據使用者登入方式(ELOAN or LDAP)
 * 選擇回傳哪種AuthenticationToken
 * 會決定由哪個Provider進行user驗證
 * </pre>
 * 
 * @since 2018年4月12日
 * @author 1607006NB01
 * @version
 *          <ul>
 *          <li>2018年4月12日,Rudy,new
 *          </ul>
 */

public class SwitchProviderFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * 根據登入方式回傳不同的Authentication(Token class不同)
     * 
     * @param request
     * @param response
     * @return Authentication
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        final String j_type = request.getParameter("j_type");
        final String j_username = request.getParameter("j_username");
        final String j_pxd = request.getParameter("j_pxd");

        UsernamePasswordAuthenticationToken authRequest;
        if ("LDAP".equals(j_type)) {
        	AuthenticationManager view = this.getAuthenticationManager();
        	ProviderManager pm;
        	if(view instanceof AuthenticationManager) {
        		pm = (ProviderManager)view;
        		for(AuthenticationProvider provider : pm.getProviders()) {
        			if(provider instanceof LdapAuthenticationProvider) {
                        LdapAuthenticationProvider lp = ((LdapAuthenticationProvider) provider);
                        //ldap參數設定security.xml
                        //如果外部參數有傳入的話，改使用外部ldap參數
                        if (request.getParameter("ldapDomain") != null) {
                            lp.setDomain(request.getParameter("ldapDomain"));
                        }
                        if (request.getParameter("ldapUrl") != null) {
                            lp.setUrl(request.getParameter("ldapUrl"));
                        }
                        if (request.getParameter("ldapRootDn") != null) {
                            lp.setRootDn(request.getParameter("ldapRootDn"));
                        }
                        if (request.getParameter("searchFilter") != null) {
                            lp.setSearchFilter(request.getParameter("searchFilter"));
                        }
                        //預設為使用為false
                        if (request.getParameter("useSSL") != null) {
                            lp.setEnableSSL(request.getParameter("useSSL"));
                        }
                        logger.debug("set LDAP domain >> " + lp.getDomain());
                        logger.debug("set LDAP url >> " + lp.getUrl());
                        logger.debug("set LDAP rootDn >> " + lp.getRootDn());
                        logger.debug("set LDAP use SSL >> " + lp.getIsSSL());
//                        final String j_username = request.getParameter("j_username");
//                        final String j_pxd = request.getParameter("j_pxd");
                        //ex:userPrincipalName：ELOANAD@tcbt.com
                        authRequest = new LdapAuthenticationToken(j_username +"@"+ lp.getDomain(), j_pxd);
                        Authentication auth = lp.authenticate(authRequest);
                        if(auth!=null) {
                            //TODO
                        }
        			}
        		}
        	}
//            authRequest = new LdapAuthenticationToken(j_username, j_pxd);
        } else {
        }

        authRequest = new CapAuthenticationToken(j_username, j_pxd);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);

    }

}
