package cn.xxywithpq.cache.caffeine;

import cn.xxywithpq.cache.FirstCache;
import cn.xxywithpq.cache.config.FirstCacheConfig;
import cn.xxywithpq.cache.provider.FirstCacheProvider;
import lombok.ToString;

import java.util.HashMap;

@ToString
public class CaffeineProvider implements FirstCacheProvider {

    private HashMap<String, CaffeineCache> caches = new HashMap<>();

    private static CaffeineProvider instance = new CaffeineProvider();

    private CaffeineProvider() {
    }

    public static CaffeineProvider getInstance() {
        return instance;
    }

    @Override
    public String name() {
        return "caffeine";
    }

    @Override
    public FirstCache buildCache(FirstCacheConfig config) {
        CaffeineConfig caffeineConfig = (CaffeineConfig) config;
        return caches.computeIfAbsent(caffeineConfig.getNamespace() + ":" + caffeineConfig.getRegion(), v -> new CaffeineCache(caffeineConfig.getSize(), caffeineConfig.getExpire(), caffeineConfig.getNamespace(), caffeineConfig.getRegion()));
    }

    @Override
    public FirstCache cache(String region) {
        return caches.get(region);
    }



}
