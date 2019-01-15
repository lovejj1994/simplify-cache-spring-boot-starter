package cn.xxywithpq.cache;

import cn.xxywithpq.cache.config.Config;
import cn.xxywithpq.cache.config.ConfigBuilder;
import cn.xxywithpq.cache.config.FirstCacheConfig;
import cn.xxywithpq.cache.redis.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.xxywithpq.cache.constants.CommonConstants.REDIS_TOPIC_DELETE;
import static cn.xxywithpq.cache.constants.CommonConstants.REDIS_TOPIC_PUT;


@Component
@Slf4j
public class SimplifyCachesPostProcessor implements BeanPostProcessor, EnvironmentAware {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisConnectionFactory connectionFactory;
    @Autowired
    RedisMessageListenerContainer redisMessageListenerContainer;

    private Environment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CachesJsonUtil) {
            log.info("SimplifyCaches init begin");
            if (null == stringRedisTemplate) {
                throw new RuntimeException("redissonClient null ,初始化失败");
            }
            CacheProviderHolder cacheProviderHolder = CacheProviderHolder.getInstance();

            List<Config> configs = ConfigBuilder.builder(environment, stringRedisTemplate);

//            需要监听的topic列表
            List<ChannelTopic> channelTopics = new ArrayList<>();
            for (Config config : configs) {
                cacheProviderHolder.init(config);
                addListener(channelTopics, config);
            }
            CachesJsonUtil cachesJsonUtil = (CachesJsonUtil) bean;
            cachesJsonUtil.setCacheProviderHolder(cacheProviderHolder);

//            初始化监听
            Map listenerParams = new HashMap(1);
            log.info("SimplifyCaches channelTopics:{}", channelTopics);
            listenerParams.put(new RedisMessageListener(cacheProviderHolder), channelTopics);
            redisMessageListenerContainer.setMessageListeners(listenerParams);
            cachesJsonUtil.setNamespace(configs.get(0).getFirstCacheConfig().getNamespace());

            log.info("SimplifyCaches init done:{}", bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    private RedisMessageListenerContainer getRedisMessageListenerContainer() {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(connectionFactory);
        return redisMessageListenerContainer;
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
}
