package com.iisigroup.cap.mvc.auth.exception;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class CapAuthenticationException extends AuthenticationException {

    private boolean captchaEnabled;

    private boolean forceChangePwd;

    private boolean askChangePwd;

    /**
     * 
     * @param msg
     */
    public CapAuthenticationException(String msg) {
        this(msg, false, false);
    }

    /**
     * 
     * @param msg
     * @param captchaEnabled
     */
    public CapAuthenticationException(String msg, boolean captchaEnabled) {
        this(msg, captchaEnabled, false);
    }

    /**
     * 
     * @param msg
     * @param captchaEnabled
     * @param forceChangePwd
     */
    public CapAuthenticationException(String msg, boolean captchaEnabled, boolean forceChangePwd) {
        this(msg, captchaEnabled, forceChangePwd, false);
    }

    /**
     * 
     * @param msg
     * @param captchaEnabled
     * @param forceChangePwd
     * @param askChangePwd
     */
    public CapAuthenticationException(String msg, boolean captchaEnabled, boolean forceChangePwd, boolean askChangePwd) {
        super(msg);
        this.captchaEnabled = captchaEnabled;
        this.forceChangePwd = forceChangePwd;
        this.askChangePwd = askChangePwd;
    }

    /**
     * 
     * @return
     */
    public boolean isCaptchaEnabled() {
        return captchaEnabled;
    }

    /**
     * 
     * @param captchaEnabled
     */
    public void setCaptchaEnabled(boolean captchaEnabled) {
        this.captchaEnabled = captchaEnabled;
    }

    /**
     * 
     * @return
     */
    public boolean isForceChangePwd() {
        return forceChangePwd;
    }

    /**
     * 
     * @param forceChangePwd
     */
    public void setforceChangePwd(boolean forceChangePwd) {
        this.forceChangePwd = forceChangePwd;
    }

    /**
     * 
     * @return
     */
    public boolean isAskChangePwd() {
        return askChangePwd;
    }

    /**
     * 
     * @param askChangePwd
     */
    public void setAskChangePwd(boolean askChangePwd) {
        this.askChangePwd = askChangePwd;
    }
}
