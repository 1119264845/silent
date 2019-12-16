package com.elfop.silent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 回显之前成功过的数据
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2019/11/6  10:31
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Silent {

    /**
     * 缓存的键值 支持sqel
     */
    String key() default "";

    /**
     * 保持的时间
     * 单位 : unit for {@link Silent#unit}.
     * 默认-1为不过期  非零正数则设定过期时间
     */
    long keep() default -1;

    /**
     * 更新内容的间隔时间
     * 单位 : unit for {@link Silent#unit}.
     * 默认10分钟 0则实时触发更新
     */
    long interval() default 10;

    /**
     * 时间单位 默认 分钟
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * 排除的缓存结果
     *
     * @return
     */
    String unless() default "";
}
