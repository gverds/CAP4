package com.iisigroup.cap.auth.handler;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.iisigroup.cap.component.impl.StringResponse;
import com.iisigroup.cap.security.model.CapUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <pre>
 * 繼承SimpleUrlAuthenticationSuccessHandler
 * 驗證成功後的Filter
 * </pre>
 * 
 * @since 2018年4月18日
 * @author 1607006NB01
 * @version
 *          <ul>
 *          <li>2018年4月18日,Rudy,new
 *          </ul>
 */

@Component("ajaxAuthenticationSuccessHandler")
public class AjaxAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 紀錄AuditLog用
    private static final String actionType = "login";

    //@Resource
    //private AuditLogService auditLogService;

    /**
     * 驗證成功後 紀錄AuditLog
     * 
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        final String j_type = request.getParameter("j_type");

        if (authentication != null) {
            CapUserDetails user = (CapUserDetails) authentication.getPrincipal();
            // For Test, after LDAP test done, must remove this part(codes)
            //auditLogService.createLog(user.getUserId(), ((CapUserDetails) user).getUnitName(), request.getRemoteAddr(), "I");
            StringBuffer sb = new StringBuffer(5000);
            sb.append("LDAP user information :" + "\n");
            sb.append("user name :" + user.getUsername() + "\n");
            sb.append("user id :" + user.getUserId() + "\n");
            if (user.getRoles() != null) {
                Map<String, String> mm = user.getRoles();
                for (String ss : mm.keySet()) {
                    sb.append("Role key : " + ss + " [" + mm.get(ss) + "]" + "\n");
                }
            }
            if (user.getMenu() != null)
                sb.append("Menu :" + user.getMenu().toString() + "\n");
            new StringResponse("text/plain", "utf-8", "user content" + sb.toString()).respond(response);
            // For Test, after LDAP test done, must remove this part(codes)
            // For Test, after LDAP test done, must unmark under line codes
            // auditLogService.createLog(user.getUserId(), request.getRemoteAddr(), "I");
        }
        /*
         * 2021/03/30,Tim,Avoid being redirected to context-root(FORBIDDEN 403) after the login page is successful
         */
        super.setUseReferer(true);
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
