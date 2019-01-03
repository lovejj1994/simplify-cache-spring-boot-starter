package cn.xxywithpq.cache;

import cn.xxywithpq.cache.caffeine.CaffeineConfig;
import cn.xxywithpq.cache.caffeine.CaffeineProvider;
import cn.xxywithpq.cache.config.Config;
import cn.xxywithpq.cache.config.FirstCacheConfig;
import cn.xxywithpq.cache.config.SecondCacheConfig;
import cn.xxywithpq.cache.provider.FirstCacheProvider;
import cn.xxywithpq.cache.provider.SecondCacheProvider;
import cn.xxywithpq.cache.redis.RedisConfig;
import cn.xxywithpq.cache.redis.RedisProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存的总控制器
 */
@Slf4j
public class CacheProviderHolder {
    private static CacheProviderHolder instance = new CacheProviderHolder();
    private FirstCacheProvider firstCacheProvider;
    private SecondCacheProvider secondCacheProvider;

    private CacheProviderHolder() {
    }

    public static CacheProviderHolder getInstance() {
        return instance;
    }

//    public CacheProviderHolder(Config config) {
//        FirstCacheConfig firstCacheConfig = config.getFirstCacheConfig();
//        SecondCacheConfig secondCacheConfig = config.getSecondCacheConfig();
//        if (firstCacheConfig instanceof CaffeineConfig) {
//            this.firstCacheProvider = CaffeineProvider.getInstance();
//        }
//        if (secondCacheConfig instanceof RedisConfig) {
//            this.secondCacheProvider = RedisProvider.getInstance();
//        }
//    }

    public FirstCache getFirstProvider(String region) {
        return firstCacheProvider.cache(region);
    }

    public SecondCache getSecondProvider(String region) {
        return secondCacheProvider.cache(region);
    }

    private FirstCache addFirstCache(FirstCacheConfig config) {
        return firstCacheProvider.buildCache(config);
    }

    private SecondCache addSecondCache(SecondCacheConfig config) {
        return secondCacheProvider.buildCache(config);
    }

    protected void init(Config config) {
        FirstCacheConfig firstCacheConfig = config.getFirstCacheConfig();
        SecondCacheConfig secondCacheConfig = config.getSecondCacheConfig();

        if (null == this.firstCacheProvider) {
            if (firstCacheConfig instanceof CaffeineConfig) {
                this.firstCacheProvider = CaffeineProvider.getInstance();
            }
        }
        if (null == this.secondCacheProvider) {
            if (secondCacheConfig instanceof RedisConfig) {
                this.secondCacheProvider = RedisProvider.getInstance();
            }
        }

        this.addFirstCache(firstCacheConfig);
        this.addSecondCache(secondCacheConfig);
    }

    @Override
    public String toString() {
        return "CacheProviderHolder{" +
                "firstCacheProvider=" + firstCacheProvider +
                ", secondCacheProvider=" + secondCacheProvider +
                '}';
    }
}
