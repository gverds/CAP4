package com.iisigroup.cap.config;

import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iisigroup.cap.base.aop.CapAuditLog4HandlerAdvice;
import com.iisigroup.cap.component.Request;

/**
 * Aspect for audit logging handlers execution
 * This aspect is a separate component that uses CapAuditLog4HandlerAdvice for actual logging
 */
@Aspect
@Component
public class AuditLogAspect {

    private final CapAuditLog4HandlerAdvice auditLog4HandlerAdvice;

    @Autowired
    public AuditLogAspect(CapAuditLog4HandlerAdvice auditLog4HandlerAdvice) {
        this.auditLog4HandlerAdvice = auditLog4HandlerAdvice;
    }

    /**
     * Pointcut for HandlerPlugin.execute method
     * @param params the parameters passed to the method
     */
    @Pointcut("execution(* com.iisigroup.cap.plugin.HandlerPlugin.execute(..)) && args(params)")
    public void handlerPluginExecute(Request params) {
        // Pointcut definition - no implementation needed
    	System.out.println("execution(* com.iisigroup.cap.plugin.HandlerPlugin.execute(..)) && args(params)");
    }

    /**
     * Around advice for handler execution
     * @param joinPoint the join point
     * @param params the parameters passed to the method
     * @return the result of the method execution
     * @throws Throwable if an error occurs
     */
    @Around("handlerPluginExecute(params)")
    public Object logAroundAjaxHandlerExecute(ProceedingJoinPoint joinPoint, Request params) throws Throwable {
        return auditLog4HandlerAdvice.logAroundAjaxHandlerExecute(joinPoint, params);
    }

    /**
     * After returning advice for handler execution
     * @param params the parameters passed to the method
     * @param reVal the return value from the method
     */
    @AfterReturning(pointcut = "handlerPluginExecute(params)", returning = "reVal")
    public void logAfterAjaxHandlerExecute(JoinPoint joinPoint, Request params, Object reVal) {
        auditLog4HandlerAdvice.logAfterAjaxHandlerExecute(joinPoint, params, reVal);
    }

    /**
     * After throwing advice for handler execution
     * @param params the parameters passed to the method
     * @param exception the exception thrown
     */
    @AfterThrowing(pointcut = "handlerPluginExecute(params)", throwing = "exception")
    public void logAfterAjaxHandlerThrowingException(JoinPoint joinPoint, Request params, Throwable exception) {
        auditLog4HandlerAdvice.logAfterAjaxHandlerThrowingException(joinPoint, params, exception);
    }
}