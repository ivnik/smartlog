package org.smartlog.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;
import org.smartlog.LogContext;
import org.smartlog.SmartLog;
import org.smartlog.SmartLogConfig;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

import javax.annotation.Nullable;

@Aspect
public class LogAspect {
    private static final Output STUB = Slf4JOutput.create()
            .withLoggerFor("stub")
            .build();

    @Nullable
    private static Loggable findLoggable(final ProceedingJoinPoint thisJoinPoint) {
        Loggable loggable = ((MethodSignature) thisJoinPoint.getSignature()).getMethod().getAnnotation(Loggable.class);
        if (loggable == null) {
            loggable = (Loggable) thisJoinPoint.getSignature().getDeclaringType().getAnnotation(Loggable.class);
        }
        return loggable;
    }

    private Class findRootDeclaringClass(final Class clazz) {
        Class curr = clazz;
        while (true) {
            Class declaringClass = curr.getDeclaringClass();
            if (declaringClass != null) {
                curr = declaringClass;
            } else {
                return curr;
            }
        }
    }

    @Around("execution(@org.smartlog.aop.Loggable * *(..))")
    @SuppressAjWarnings({"adviceDidNotMatch"})
    public Object aroundLoggable(final ProceedingJoinPoint thisJoinPoint) throws Throwable {
        final Loggable loggable = findLoggable(thisJoinPoint);

        if (loggable == null) {
            SmartLogConfig.getConfig()
                    .getInternalErrorHandler()
                    .accept("@Loggable not found for: " + thisJoinPoint.getSignature().getDeclaringType());
        }

        final LogContext ctx = SmartLog.start(STUB);

        if (loggable != null) {
            ctx.level(loggable.defaultLevel());
        }

        LoggableCallback callback = null;
        try {
            if (thisJoinPoint.getTarget() != null && thisJoinPoint.getTarget() instanceof LoggableCallback) {
                callback = (LoggableCallback) thisJoinPoint.getTarget();
            }

            if (callback != null) {
                callback.beforeLoggable();
            }

            // call method
            final Object ret = thisJoinPoint.proceed();

            if (callback != null) {
                callback.afterLoggable();
            }

            if (ctx.result() == null) {
                ctx.result(ret);
            }

            return ret;
        } catch (Throwable t) {
            if (callback != null) {
                callback.afterLoggable();
            }

            ctx.throwable(t);

            throw t;
        } finally {
            try {
                // use method name if title is not set
                if (ctx.title() == null) {
                    ctx.title(thisJoinPoint.getSignature().getName());
                }
            } finally {
                if (ctx.output() == STUB) {
                    final Class clazz = findRootDeclaringClass(thisJoinPoint.getSignature().getDeclaringType());
                    final Output output = Slf4JOutput.create()
                            .withLoggerFor(clazz)
                            .build();

                    ctx.output(output);
                }

                SmartLog.finish();
            }
        }
    }
}