# silent
+ 在想要缓存的的方法上加上注解 当第一次成功执行方法之后 会将返回值在redis存放缓存 在后续读取缓存的数据 </br>
+ 当超过设定的刷新时间后 获得缓存的数据后会启动后台线程更新缓存的数据 </br>
+ 目前依赖spring-boot2.x的redis与aop模块 

### 使用说明
通过maven引用
``` java
        <dependency>
            <groupId>org.elfop</groupId>
            <artifactId>silent</artifactId>
            <version>1.0-snapshot</version>
        </dependency>
```
设定bean配置使用 指定redisTemplate,可选指定后台刷新数据的线程池
```java
@Configuration
public class SilentConfig {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private ThreadPoolExecutor poolExecutor;

    //@Bean
    //public SilentPointcut silentPointcut(){
    //    return new SilentPointcut(redisTemplate);
    //}
    
    @Bean
    public SilentPointcut silentPointcut(){
        return new SilentPointcut(redisTemplate,poolExecutor);
    }
}

```

在需要的方法上加上注解 **@Silent** </br>
```java
    @Silent(key = "#req.id", unless = "#root.data eq null")
    @PostMapping("/demo")
    public WebResult findOperationsDisplay(@RequestBody DemoReq req) {
        return WebResult.okResult();
    }
```
注解值设定缓存的键值或过期时间
```java

    /**
     * 缓存的键值 支持sqel,  为空时根据方法参数自动设定缓存的key值
     */
    String key() default "";

    /**
     * 缓存数据保持的时间
     * 单位 : unit for {@link Silent#unit}.
     * 默认-1为不过期  非零正数则设定过期时间
     */
    long keep() default -1;

    /**
     * 当方法拥有缓存数据后 设定下次更新内容的间隔时间
     * 单位 : unit for {@link Silent#unit}.
     * 默认10分钟 0则实时触发更新
     */
    long interval() default 10;

    /**
     * 时间单位 默认 分钟
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * spel条件
     * 排除的缓存结果  可以为空 根据spel进行判断是否缓存
     *
     * @return
     */
    String unless() default "";
```
> * todo 后期剔除依赖的spring-boot2.x的模块 更改为独立模块
