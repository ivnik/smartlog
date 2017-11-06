package org.smartlog.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.smartlog.LogContext;
import org.smartlog.SmartLog;
import org.smartlog.SmartLogConfig;
import org.smartlog.Util;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

import javax.annotation.Nonnull;

@Aspect
public class LogAspect {
    private static final Output STUB = Slf4JOutput.create()
            .withLoggerFor("stub")
            .build();

    @Nonnull
    private static Loggable findLoggable(final JoinPoint joinPoint) {
        Loggable loggable = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Loggable.class);
        if (loggable == null) {
            loggable = (Loggable) joinPoint.getSignature().getDeclaringType().getAnnotation(Loggable.class);
        }

        if (loggable == null) {
            throw new RuntimeException("Internal error. No @Loggable found for: " + joinPoint.getSignature());
        }

        return loggable;
    }

    @Before("execution(@org.smartlog.aop.Loggable * *(..))")
    public void beforeLoggable(final JoinPoint joinPoint) throws Throwable {
        final Loggable loggable = findLoggable(joinPoint);

        final LogContext ctx = SmartLog.start(STUB, joinPoint.getTarget());
        if (ctx.level() == null) {
            ctx.level(loggable.defaultLevel());
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
        // use method name if title is not set
        if (ctx.title() == null) {
            ctx.title(joinPoint.getSignature().getName());
        }

        // use default output if @Loggable method didn't change output
        if (ctx.output() == STUB) {
            final Class clazz = Util.findRootEnclosingClass(joinPoint.getSignature().getDeclaringType());
            final Output output = SmartLogConfig.getConfig().getDefaultOutput(clazz);

            ctx.output(output);
        }

        SmartLog.finish();
    }
}