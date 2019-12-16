package com.elfop.silent.perform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2019/12/9  13:36
 */
public class CacheHandler {

    public static final String REFRESH_DATA_INTERVAL = "c.j.a.b.s.p.refresh";
    public static final String DATA_CACHE = "silent_data_cache";
    private static final Log log = LogFactory.getLog(CacheHandler.class);
    private final RedisTemplate<String, Object> silentRedisTemplate;
    private ThreadPoolExecutor silentPoolExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CacheHandler(RedisTemplate<String, Object> silentRedisTemplate, ThreadPoolExecutor threadPoolExecutor) {
        Assert.notNull(silentRedisTemplate, "silentRedisTemplate cannot be null");
        Assert.notNull(silentRedisTemplate, "silentPoolExecutor cannot be null");
        this.silentRedisTemplate = silentRedisTemplate;
        this.silentPoolExecutor = threadPoolExecutor;
    }


    public Object execute(CacheHandlerContext context) throws Throwable {

        Object result = serviceCall(context);

        cacheDate(context, result);

        return result;
    }

    public Object cacheData(CacheHandlerContext context) {

        Object result = pointProceed(context);

        refreshValue(context);

        return result;
    }

    public boolean lazyLoading(String key, String methodName) {
        return silentRedisTemplate.hasKey(methodName.concat(DATA_CACHE).concat(key));
    }

    private void cacheDate(CacheHandlerContext context, Object result) {

        silentPoolExecutor.execute(() -> backGroundBackup(context, result));
    }

    private void refreshValue(CacheHandlerContext context) {

        if (!isRefresh(context)) {

            silentPoolExecutor.execute(() -> {
                try {

                    backGroundBackup(context, serviceCall(context));
                } catch (Throwable throwable) {
                    log.error("cacheData backGroundBackup ERROR : :" + throwable + "  : " + throwable.getMessage());
                }
            });

        }

    }

    private Object serviceCall(CacheHandlerContext context) throws Throwable {
        Object result = context.getJoinPoint().proceed();

        String intervalKey = context.getMethodName().concat(REFRESH_DATA_INTERVAL).concat(context.getKey());
        silentRedisTemplate.opsForValue().set(intervalKey, 1);
        setExpire(intervalKey, context.getInterval(), context.getUnit());

        return result;
    }


    private boolean isRefresh(CacheHandlerContext context) {

        String intervalKey = context.getMethodName().concat(REFRESH_DATA_INTERVAL).concat(context.getKey());
        String dataKey = context.getMethodName()
                .concat(DATA_CACHE).concat(context.getKey());

        boolean intervalFlag = silentRedisTemplate.hasKey(intervalKey);
        boolean dataFlag = silentRedisTemplate.hasKey(dataKey);

        return intervalFlag && dataFlag;
    }


    private void backGroundBackup(CacheHandlerContext context, Object result) {

        String cacheKey = context.getMethodName().concat(DATA_CACHE).concat(context.getKey());

        if (!isUnless(context, result)) {
            silentRedisTemplate.opsForValue().set(cacheKey, result);
            setExpire(cacheKey, context.getKeep(), context.getUnit());
        }

    }

    private Object pointProceed(CacheHandlerContext context) {

        return silentRedisTemplate.opsForValue().get(context.getMethodName().concat(DATA_CACHE).concat(context.getKey()));
    }

    private Boolean isUnless(CacheHandlerContext context, Object result) {

        if (isBlank(context.getUnless())) {
            return false;
        }

        SpelParserConfiguration config = new SpelParserConfiguration(true, true);
        ExpressionParser parser = new SpelExpressionParser(config);
        EvaluationContext evaluationContext = new StandardEvaluationContext(result);

        Expression expression = parser.parseExpression(context.getUnless());

        return expression.getValue(evaluationContext, Boolean.class);
    }


    private void setExpire(String key, long number, TimeUnit unit) {
        if (number > 0) {
            silentRedisTemplate.expire(key, number, unit);
        }
    }

}
