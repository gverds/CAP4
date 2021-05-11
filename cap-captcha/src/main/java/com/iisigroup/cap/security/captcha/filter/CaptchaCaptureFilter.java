package com.iisigroup.cap.security.captcha.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class CaptchaCaptureFilter extends OncePerRequestFilter {

    private String userCaptchaResponse;

    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        logger.debug("Captcha capture filter");

        // Assign values only when user has submitted a Captcha value.
        // Without this condition the values will be reset due to redirection
        // and CaptchaVerifierFilter will enter an infinite loop

        synchronized (req) {
            if (!StringUtils.isBlank(req.getParameter("captcha"))) {
                userCaptchaResponse = req.getParameter("captcha");
            }
            logger.debug("userResponse: " + req.getParameter("captcha"));
        }

        logger.debug("userResponse: " + userCaptchaResponse);

        // Proceed with the remaining filters
        chain.doFilter(req, res);
    }

    public String getUserCaptchaResponse() {
        return userCaptchaResponse;
    }

    public void setUserCaptchaResponse(String userCaptchaResponse) {
        this.userCaptchaResponse = userCaptchaResponse;
    }
}