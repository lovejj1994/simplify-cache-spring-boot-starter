package cn.xxywithpq.cache;

import cn.xxywithpq.cache.caffeine.CaffeineConfig;
import cn.xxywithpq.cache.config.Config;
import cn.xxywithpq.cache.config.FirstCacheConfig;
import cn.xxywithpq.cache.redis.RedisConfig;
import cn.xxywithpq.cache.redis.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.xxywithpq.cache.constants.CommonConstants.*;


@Slf4j
public class CachesJsonUtilFactoryBean implements FactoryBean<CachesJsonUtil>, EnvironmentAware {

    private static final String CONFIG_REGION_PREFIX = "simplify.cache.region.";
    private static final String CONFIG_NAMESPACE_PREFIX = "simplify.cache.namespace";
    private static final String CONFIG_SYNC_REDIS_TTL_PREFIX = "simplify.cache.sync-redis-ttl.";
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisConnectionFactory connectionFactory;
    @Autowired
    RedisMessageListenerContainer redisMessageListenerContainer;

    private Environment environment;

    public CachesJsonUtilFactoryBean() {
    }


    @Override
    public CachesJsonUtil getObject() {
        long start = System.currentTimeMillis();
        CachesJsonUtil cachesJsonUtil = new CachesJsonUtil();
        log.info("SimplifyCaches init begin");
        if (null == stringRedisTemplate) {
            throw new RuntimeException("redissonClient null ,初始化失败");
        }

        CacheProviderHolder cacheProviderHolder = CacheProviderHolder.getInstance();
        String namespace;
        String region = DEFAULT_REGION;

//            Spring boot 2以下用 RelaxedPropertyResolver绑定
        RelaxedPropertyResolver relaxedPropertyResolver = new RelaxedPropertyResolver(environment);
        //从配置文件查找namespace
        namespace = relaxedPropertyResolver.getProperty(CONFIG_NAMESPACE_PREFIX);
        log.info("namespace {}", namespace);


        Map<String, Object> ttl = relaxedPropertyResolver.getSubProperties(CONFIG_SYNC_REDIS_TTL_PREFIX);
        if (!ttl.isEmpty()) {
            for (Map.Entry<String, Object> entry : ttl.entrySet()) {
                log.info("ttl.getKey() {} , ttl.getValue().toString() {}", entry.getKey(), entry.getValue().toString());
            }
        }

        CaffeineConfig firstCacheConfig = new CaffeineConfig();
        RedisConfig secondCacheConfig = new RedisConfig();
//            需要监听的topic列表
        List<ChannelTopic> channelTopics = new ArrayList<>();
        //从配置文件查找region
        Map<String, Object> regions = relaxedPropertyResolver.getSubProperties(CONFIG_REGION_PREFIX);
        if (!regions.isEmpty()) {
            for (Map.Entry<String, Object> entry : regions.entrySet()) {
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

                Config config = new Config();
                config.setFirstCacheConfig(firstCacheConfig);
                config.setSecondCacheConfig(secondCacheConfig);
                cacheProviderHolder.init(config);
                addListener(channelTopics, config);
            }
//                region没有指定，用默认值
        } else {
            firstCacheConfig.setRegion(region);
            secondCacheConfig.setRegion(region);
            firstCacheConfig.setSize(DEFAULT_SIZE);
            firstCacheConfig.setExpire(DEFAULT_EXPIRE);
            secondCacheConfig.setExpire(DEFAULT_EXPIRE);

            Config config = new Config();
            config.setFirstCacheConfig(firstCacheConfig);
            config.setSecondCacheConfig(secondCacheConfig);
            cacheProviderHolder.init(config);

            addListener(channelTopics, config);
        }
        cachesJsonUtil.setCacheProviderHolder(cacheProviderHolder);

//            初始化监听
        initRedisMessageListenerContainer(channelTopics, new RedisMessageListener(cacheProviderHolder));

        cachesJsonUtil.setNamespace(namespace);

        log.info("SimplifyCaches init done:{} , last {} ms", cachesJsonUtil, System.currentTimeMillis() - start);
        return cachesJsonUtil;
    }

    @Override
    public Class<?> getObjectType() {
        return CachesJsonUtil.class;
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * 收集监听列表
     *
     * @param channelTopics
     * @param config
     */
    private void addListener(List<ChannelTopic> channelTopics, Config config) {
        FirstCacheConfig firstCacheConfig = config.getFirstCacheConfig();
        ChannelTopic putChannelTopic = new ChannelTopic(REDIS_TOPIC_PUT + firstCacheConfig.getNamespace() + ":" + firstCacheConfig.getRegion());
        ChannelTopic deleteChannelTopic = new ChannelTopic(REDIS_TOPIC_DELETE + firstCacheConfig.getNamespace() + ":" + firstCacheConfig.getRegion());
        channelTopics.add(putChannelTopic);
        channelTopics.add(deleteChannelTopic);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    private void initRedisMessageListenerContainer(List<ChannelTopic> channelTopics, RedisMessageListener redisMessageListener) {
        Map listenerParams = new HashMap(1);
        log.info("SimplifyCaches channelTopics:{}", channelTopics);
        listenerParams.put(redisMessageListener, channelTopics);

        redisMessageListenerContainer.setMessageListeners(listenerParams);
        redisMessageListenerContainer.afterPropertiesSet();
        redisMessageListenerContainer.start();
    }
}
