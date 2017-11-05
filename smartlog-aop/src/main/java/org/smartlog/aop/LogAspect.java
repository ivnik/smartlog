package org.smartlog.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.smartlog.LogContext;
import org.smartlog.SmartLog;
import org.smartlog.SmartLogConfig;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Aspect
public class LogAspect {
    private static final Output STUB = Slf4JOutput.create()
            .withLoggerFor("stub")
            .build();

    @Nullable
    private static Loggable findLoggable(final JoinPoint joinPoint) {
        Loggable loggable = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Loggable.class);
        if (loggable == null) {
            loggable = (Loggable) joinPoint.getSignature().getDeclaringType().getAnnotation(Loggable.class);
        }
        return loggable;
    }

    @Nonnull
    private static Class findRootDeclaringClass(final Class clazz) {
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

    @Before("execution(@org.smartlog.aop.Loggable * *(..))")
    public void beforeLoggable(final JoinPoint  joinPoint) throws Throwable {
        final Loggable loggable = findLoggable(joinPoint);

        if (loggable == null) {
            throw new RuntimeException("Internal error. No @Loggable found for: " + joinPoint.getSignature());
        }

        SmartLog.start(STUB)
                .level(loggable.defaultLevel());

        if (joinPoint.getTarget() != null && joinPoint.getTarget() instanceof LoggableCallback) {
            LoggableCallback callback = (LoggableCallback) joinPoint.getTarget();
            callback.beforeLoggable();
        }
    }

    @AfterReturning(value = "execution(@org.smartlog.aop.Loggable * *(..))", returning = "ret")
    public void afterReturiningLoggable(final JoinPoint joinPoint, final Object ret) throws Throwable {
        final LogContext ctx = SmartLog.current();
        if (ctx.result() == null) {
            ctx.result(ret);
        }
        finish(joinPoint, ctx);
    }


    @AfterThrowing(value = "execution(@org.smartlog.aop.Loggable * *(..))", throwing = "t")
    public void afterThrowingLoggable(final JoinPoint joinPoint, final Throwable t) throws Throwable {
        final LogContext ctx = SmartLog.throwable(t);
        finish(joinPoint, ctx);
    }

    private void finish(final JoinPoint joinPoint, final LogContext ctx) {
        try {
            if (joinPoint.getTarget() != null && joinPoint.getTarget() instanceof LoggableCallback) {
                final LoggableCallback callback = (LoggableCallback) joinPoint.getTarget();
                callback.afterLoggable();
            }
        } catch (Throwable t) {
            ctx.throwable(t);
            throw t;
        } finally {
            // use method name if title is not set
            if (ctx.title() == null) {
                ctx.title(joinPoint.getSignature().getName());
            }

            if (ctx.output() == STUB) {
                // use default output if @Loggable method didn't change output
                final Class clazz = findRootDeclaringClass(joinPoint.getSignature().getDeclaringType());
                final Output output = SmartLogConfig.getConfig().getDefaultOutput(clazz);

                ctx.output(output);
            }

            SmartLog.finish();
        }
    }
}