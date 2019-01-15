package cn.xxywithpq.cache.config;

import cn.xxywithpq.cache.caffeine.CaffeineConfig;
import cn.xxywithpq.cache.redis.RedisConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.xxywithpq.cache.constants.CommonConstants.*;

@Data
@Slf4j
public class ConfigBuilder {

    private static final ConfigurationPropertyName CONFIG_REGION_PREFIX = ConfigurationPropertyName
            .of("simplify.cache.region");
    private static final ConfigurationPropertyName CONFIG_NAMESPACE_PREFIX = ConfigurationPropertyName
            .of("simplify.cache.namespace");
    private static final ConfigurationPropertyName CONFIG_SYNC_REDIS_TTL_PREFIX = ConfigurationPropertyName
            .of("simplify.cache.sync-redis-ttl");
    private static final Bindable<Map<String, Object>> STRING_OBJECT_MAP = Bindable
            .mapOf(String.class, Object.class);
    private static final Bindable<String> STRING = Bindable
            .of(String.class);

    public static List<Config> builder(Environment environment, StringRedisTemplate stringRedisTemplate) {
        if (null == environment) {
            log.error("environment 为空,simplify-cache初始化失败");
            return null;
        }

        if (null == stringRedisTemplate) {
            log.error("redis 连接失败 ,请查看配置");
            return null;
        }
        ArrayList<Config> configs = new ArrayList<>();

        String namespace;
        String region = DEFAULT_REGION;

        Binder binder = Binder.get(environment);
        namespace = binder.bind(CONFIG_NAMESPACE_PREFIX, STRING).orElse(DEFAULT_NAMESPACE);
        log.info("namespace {}", namespace);

        Map<String, Object> ttl = binder.bind(CONFIG_SYNC_REDIS_TTL_PREFIX, STRING_OBJECT_MAP).orElseGet(Collections::emptyMap);
        if (!ttl.isEmpty()) {
            for (Map.Entry<String, Object> entry : ttl.entrySet()) {
                log.info("ttl.getKey() {} , ttl.getValue().toString() {}", entry.getKey(), entry.getValue().toString());
            }
        }
        Config config = new Config();
        CaffeineConfig firstCacheConfig = new CaffeineConfig();
        RedisConfig secondCacheConfig = new RedisConfig();

        //从配置文件查找region
        Map<String, Object> regions = binder.bind(CONFIG_REGION_PREFIX, Bindable.mapOf(String.class, Object.class)).orElseGet(Collections::emptyMap);
        if (!regions.isEmpty()) {
            for (Map.Entry<String, Object> entry : regions.entrySet()) {
                config = new Config();
                firstCacheConfig = new CaffeineConfig();
                secondCacheConfig = new RedisConfig();
                firstCacheConfig.setNamespace(namespace);
                secondCacheConfig.setNamespace(namespace);
                secondCacheConfig.setRedisTemplate(stringRedisTemplate);

                log.info("regions.getKey() {} , regions.getValue().toString() {}", entry.getKey(), entry.getValue().toString());
                String key = entry.getKey();

                if (StringUtils.isNotBlank(key)) {
                    region = key;
                }
                firstCacheConfig.setRegion(region);
                secondCacheConfig.setRegion(region);
                String value = entry.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    String[] split = value.split(",");
                    if (split.length > 1) {
                        firstCacheConfig.setSize(Long.valueOf(split[0]));
                        firstCacheConfig.setExpire(Long.valueOf(split[1]));
                    }
                } else {
                    firstCacheConfig.setSize(DEFAULT_SIZE);
                    firstCacheConfig.setExpire(DEFAULT_EXPIRE);
                    secondCacheConfig.setExpire(DEFAULT_EXPIRE);
                }

                //如果sync_redis_ttl同步为true,则把一级缓存的过期时长同步到二级缓存中
                if (!ttl.isEmpty()) {
                    Object ttlObj = ttl.get(region);
                    if (null != ttlObj) {
                        boolean judge = Boolean.valueOf((String) ttlObj);
                        if (judge) {
                            secondCacheConfig.setExpire(firstCacheConfig.getExpire());
                        }
                    }
                }

                config.setFirstCacheConfig(firstCacheConfig);
                config.setSecondCacheConfig(secondCacheConfig);
                configs.add(config);
            }
//                region没有指定，用默认值
        } else {
            firstCacheConfig.setRegion(region);
            secondCacheConfig.setRegion(region);
            firstCacheConfig.setSize(DEFAULT_SIZE);
            firstCacheConfig.setExpire(DEFAULT_EXPIRE);
            secondCacheConfig.setExpire(DEFAULT_EXPIRE);

            config.setFirstCacheConfig(firstCacheConfig);
            config.setSecondCacheConfig(secondCacheConfig);
            configs.add(config);
        }
        return configs;
    }
}
