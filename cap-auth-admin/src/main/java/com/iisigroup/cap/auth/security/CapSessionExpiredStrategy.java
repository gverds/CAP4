package com.iisigroup.cap.auth.security;

import java.io.IOException;

import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CapSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    private final RedirectStrategy redirectStrategy;

    public CapSessionExpiredStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();

        // 執行自訂的導向邏輯
        redirectStrategy.sendRedirect(request, response, "/j_spring_security_logout");
    }
}