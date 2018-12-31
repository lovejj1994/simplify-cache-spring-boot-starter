package cn.xxywithpq.cache.constants;


public class CommonConstants {
    /**
     * 判断是   否
     */
    public static final String JSON_KEY = "key";
    public static final String JSON_VALUE = "valueStr";

    /**
     * 默认容量大小
     */
    public static final Long DEFAULT_SIZE = 1000L;
    /**
     * 默认namespace
     */
    public static final String DEFAULT_NAMESPACE = "defaultNamespace";
    /**
     * 默认namespace
     */
    public static final String DEFAULT_REGION = "defaultRegion";

    /**
     * 默认过期时长（秒）
     */
    public static final Long DEFAULT_EXPIRE = 300000L;


    public static final String REDIS_TOPIC_PUT = "redisCache:putTopic:";
    public static final String REDIS_TOPIC_DELETE = "redisCache:deleteTopic:";


}
