package com.elfop.silent.perform;

import com.elfop.silent.annotation.Silent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2019/12/9  15:23
 */
public class CacheHandlerContext {

    private String key;

    private ProceedingJoinPoint joinPoint;

    private long keep;

    private long interval;

    private TimeUnit unit;

    private String unless;

    private String methodName;

    public CacheHandlerContext(String key, ProceedingJoinPoint joinPoint, Silent silent) {
        this.key = key;
        this.joinPoint = joinPoint;
        this.keep = silent.keep();
        this.interval = silent.interval();
        this.unit = silent.unit();
        this.unless = silent.unless();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        this.methodName = methodSignature.getMethod().getName();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ProceedingJoinPoint getJoinPoint() {
        return joinPoint;
    }

    public void setJoinPoint(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public long getKeep() {
        return keep;
    }

    public void setKeep(long keep) {
        this.keep = keep;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public String getUnless() {
        return unless;
    }

    public void setUnless(String unless) {
        this.unless = unless;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
