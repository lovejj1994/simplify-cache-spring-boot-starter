package cn.xxywithpq.cache.redis;

import cn.xxywithpq.cache.CacheProviderHolder;
import cn.xxywithpq.cache.constants.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import static cn.xxywithpq.cache.constants.CommonConstants.REDIS_TOPIC_DELETE;
import static cn.xxywithpq.cache.constants.CommonConstants.REDIS_TOPIC_PUT;

/**
 * @program: simplify-cache-spring-boot-starter
 * @description: ${description}
 * @author: qian.pan
 * @create: 2018/12/31 16:40
 **/
@Slf4j
public class RedisMessageListener implements MessageListener {

    CacheProviderHolder cacheProviderHolder;

    public RedisMessageListener(CacheProviderHolder cacheProviderHolder) {
        this.cacheProviderHolder = cacheProviderHolder;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        if (channel.startsWith(REDIS_TOPIC_PUT)) {
            String region = channel.substring(channel.indexOf(REDIS_TOPIC_PUT));
            log.info("RedisCache first cache put listener begin channel:{} key:{}", channel, message);
            JSONObject jsonObject = JSONObject.parseObject(body);
            Object result = cacheProviderHolder.getFirstProvider(region).put(jsonObject.getString(CommonConstants.JSON_KEY), jsonObject.getString(CommonConstants.JSON_VALUE));
            log.info("RedisCache first cache put listener result channel:{}  key:{} result:{}", channel, message, result);
        } else if (channel.startsWith(REDIS_TOPIC_DELETE)) {
            String region = channel.substring(channel.indexOf(REDIS_TOPIC_DELETE));
            log.info("RedisCache first cache delete listener begin channel:{}  key:{}", channel, message);
            Object result = cacheProviderHolder.getFirstProvider(region).delete(body);
            log.info("RedisCache first cache delete listener result channel:{} key:{} result:{}", channel, message, result);
        }
    }
}
