package cn.xxywithpq.cache.provider;


import cn.xxywithpq.cache.SecondCache;
import cn.xxywithpq.cache.config.SecondCacheConfig;

public interface SecondCacheProvider extends CacheProvider {

    SecondCache cache(String region);

    /**
     * Configure the cache
     *
     * @return return cache instance
     */
    SecondCache buildCache(SecondCacheConfig config);
}
