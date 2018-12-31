package cn.xxywithpq.cache.provider;


import cn.xxywithpq.cache.FirstCache;
import cn.xxywithpq.cache.config.FirstCacheConfig;

public interface FirstCacheProvider extends CacheProvider {

    FirstCache cache(String region);

    /**
     * Configure the cache
     *
     * @param regionName the name of the cache region
     * @param listener   listener for expired elements
     * @return return cache instance
     */
    FirstCache buildCache(FirstCacheConfig config);
}
