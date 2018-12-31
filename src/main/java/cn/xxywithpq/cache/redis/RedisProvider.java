package cn.xxywithpq.cache.redis;

import cn.xxywithpq.cache.SecondCache;
import cn.xxywithpq.cache.config.SecondCacheConfig;
import cn.xxywithpq.cache.provider.SecondCacheProvider;
import lombok.ToString;

import java.util.HashMap;

@ToString
public class RedisProvider implements SecondCacheProvider {

    private HashMap<String, RedisCache> caches = new HashMap<>();

    private static RedisProvider instance = new RedisProvider();

    private RedisProvider() {
    }

    public static RedisProvider getInstance() {
        return instance;
    }

    @Override
    public String name() {
        return "redis";
    }

    @Override
    public SecondCache buildCache(SecondCacheConfig config) {
        RedisConfig redisConfig = (RedisConfig) config;
        return caches.computeIfAbsent(redisConfig.getNamespace() + ":" + redisConfig.getRegion(), v -> new RedisCache(redisConfig.getRedisTemplate(), redisConfig.getRegion(), redisConfig.getNamespace(), redisConfig.getExpire()));
    }

    @Override
    public SecondCache cache(String region) {
        return caches.get(region);
    }
}
