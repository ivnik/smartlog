package org.smartlog.aop;

import org.smartlog.LogLevel;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {
    LogLevel defaultLevel() default LogLevel.INFO;
}