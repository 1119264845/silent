package com.elfop.silent;

import com.elfop.silent.annotation.Silent;
import com.elfop.silent.perform.CacheHandler;
import com.elfop.silent.perform.CacheHandlerContext;
import com.elfop.silent.thread.ThreadPoolExecutorConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2019/12/6  11:15
 */
@Aspect
@Configuration
public class SilentPointcut {

    private static final Log log = LogFactory.getLog(SilentPointcut.class);
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private CacheHandler cacheHandler;
    /**
     * 用于SpEL表达式解析.
     */
    private SpelExpressionParser parser = new SpelExpressionParser();

    private SilentPointcut() {
    }

    public SilentPointcut(RedisTemplate<String, Object> silentRedisTemplate) {
        init(silentRedisTemplate, null);
    }

    public SilentPointcut(RedisTemplate<String, Object> silentRedisTemplate, ThreadPoolExecutor silentPoolExecutor) {
        init(silentRedisTemplate, silentPoolExecutor);
    }

    private void init(RedisTemplate<String, Object> silentRedisTemplate, ThreadPoolExecutor silentPoolExecutor) {

        ThreadPoolExecutor threadPoolExecutor;

        if (silentPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutorConf().silentPoolExecutor();
        } else {
            threadPoolExecutor = silentPoolExecutor;
        }

        this.cacheHandler = new CacheHandler(silentRedisTemplate, threadPoolExecutor);
    }

    @Around("@annotation(silent)")
    public Object doThrough(ProceedingJoinPoint joinPoint, Silent silent) {

        Object result;
        String key = generateKey(joinPoint, silent);
        CacheHandlerContext handlerContext = new CacheHandlerContext(key, joinPoint, silent);

        try {
            if (cacheHandler.lazyLoading(key, handlerContext.getMethodName())) {
                result = cacheHandler.cacheData(handlerContext);
            } else {
                result = cacheHandler.execute(handlerContext);
            }
        } catch (Throwable throwable) {
            result = null;
            log.error("SilentPointcut ERROR :" + throwable + "  : " + throwable.getMessage());
        }

        return result;
    }

    /**
     * 默认缓存key生成器.
     * 注解中key不传参，根据方法签名和参数生成key.
     *
     * @param joinPoint
     * @return
     */
    private String generateKey(ProceedingJoinPoint joinPoint, Silent silent) {
        if (isBlank(silent.key())) {
            return defaultKey(joinPoint);
        }

        return spElKey(silent.key(), joinPoint);
    }

    private String defaultKey(ProceedingJoinPoint joinPoint) {
        Class itsClass = joinPoint.getTarget().getClass();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(itsClass.getName());
        keyBuilder.append(".").append(methodSignature.getName());
        keyBuilder.append("(");
        for (Object arg : joinPoint.getArgs()) {
            keyBuilder.append(arg.getClass().getSimpleName()).append(arg).append(";");
        }
        keyBuilder.append(")");
        return keyBuilder.toString();
    }

    private String spElKey(String key, ProceedingJoinPoint joinPoint) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(methodSignature.getMethod());
        Expression expression = parser.parseExpression(key);
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            context.setVariable(Objects.requireNonNull(paramNames, "cannot be null")[i], args[i]);
        }
        return String.valueOf(expression.getValue(context));

    }

}
