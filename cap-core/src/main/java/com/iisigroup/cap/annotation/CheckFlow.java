package com.iisigroup.cap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * Check Flow Sched
 * </pre>
 * 
 * @since 2023/01/05
 * @author AllenChiu
 * @version
 *          <ul>
 *          <li>2023/01/05,AllenChiu,new
 *          </ul>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckFlow {
    String name() default "";

}
