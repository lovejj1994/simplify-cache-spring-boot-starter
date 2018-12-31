/**
 * Copyright (c) 2015-2017, Winter Lau (javayou@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xxywithpq.cache.redis;

import cn.xxywithpq.cache.FirstCache;
import cn.xxywithpq.cache.SecondCache;
import cn.xxywithpq.cache.constants.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache
 *
 * @author Winter Lau(javayou@gmail.com)
 */
@Data
@Slf4j
public class RedisCache implements SecondCache {

    private RedissonClient redissonClient;
    private RedisTemplate<String, String> redisTemplate;

    private ValueOperations<String, String> valueOperations;

    private Long expire;
    private String region;
    private String namespace;

    private final String redisTopicPut = "redisCache:putTopic:";
    private final String redisTopicDelete = "redisCache:deleteTopic:";

//    private MessageListenerAdapter messageListenerAdapter;

    /**
     * @param redisTemplate
     * @param namespace     命名空间，一般指不同应用名
     * @param region        区域
     * @param expire
     */
    public RedisCache(RedisTemplate redisTemplate, String region, String namespace, Long expire) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.region = region;
        this.namespace = namespace;
        this.expire = expire;
//        this.messageListenerAdapter = new MessageListenerAdapter(new RedisMessageListener());
//        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
    }

    @Override
    public Object put(String key, String value) {
//        RBucket<String> bucket = redissonClient.getBucket(key(key));
        if (null != expire) {
            this.valueOperations.set(key(key), value, expire, TimeUnit.MILLISECONDS);
//            bucket.set(value, expire, TimeUnit.MILLISECONDS);
        } else {
            this.valueOperations.set(key(key), value);
//            bucket.set(value);
        }
        return null;
    }

    @Override
    public Object delete(String key) {
        Boolean delete = this.redisTemplate.delete(key(key));
//        this.valueOperations.decrement()
//        RBucket<Object> bucket = redissonClient.getBucket(key(key));
        return delete;
    }

    @Override
    public Object get(String key) {
        String value = this.valueOperations.get(key(key));

//        RBucket<Object> bucket = redissonClient.getBucket(key(key));
        return value;
    }

    @Override
    public void syncPutFirstCacheJson(String region, String key, String valueStr) {
        RTopic topic = redissonClient.getTopic(redisTopicPut + this.namespace + ":" + region);
        JSONObject message = new JSONObject(2);
        message.put(CommonConstants.JSON_KEY, key);
        message.put(CommonConstants.JSON_VALUE, valueStr);
        long clientsReceivedMessage = topic.publish(message.toJSONString());
        log.info("syncFirstCacheJson, namespace:{} region:{} key: {} value: {}, {}个实例接收到信息", this.namespace, region, key, valueStr, clientsReceivedMessage);
    }

    @Override
    public void syncDeleteFirstCache(String region, String key) {
        RTopic topic = redissonClient.getTopic(redisTopicDelete + this.namespace + ":" + region);
        long clientsReceivedMessage = topic.publish(key(key));
        log.info("syncDeleteFirstCache,namespace:{} region:{} key:{}, {}个实例接收到信息", this.namespace, region, key, clientsReceivedMessage);
    }

    @Override
    public String key(String key) {
        return this.namespace + ":" + this.region + ":" + key;
    }

    @Override
    public void initClearFirstCacheListener(FirstCache firstCache) {
        RTopic<String> deleteTopic = redissonClient.getTopic(this.redisTopicDelete + this.namespace + ":" + this.region);
        deleteTopic.addListener((channel, message) -> {
            log.info("RedisCache first cache delete listener begin channel:{} namespace:{} region:{} key:{}", channel, this.namespace, this.region, message);
            Object result = firstCache.delete(message);
            log.info("RedisCache first cache delete listener result channel:{} namespace:{} region:{} key:{} result:{}", channel, this.namespace, this.region, message, result);
        });
    }

    @Override
    public void initPutFirstCacheListener(FirstCache firstCache) {
        RTopic<String> putTopic = redissonClient.getTopic(this.redisTopicPut + this.namespace + ":" + this.region);
        putTopic.addListener((channel, message) -> {
            if (StringUtils.isNotBlank(message)) {
                log.info("RedisCache first cache put listener begin channel:{} namespace:{} region:{} key:{}", channel, this.namespace, this.region, message);
                JSONObject jsonObject = JSONObject.parseObject(message);
                Object result = firstCache.put(jsonObject.getString(CommonConstants.JSON_KEY), jsonObject.getString(CommonConstants.JSON_VALUE));
                log.info("RedisCache first cache put listener result channel:{} namespace:{} region:{} key:{} result:{}", channel, this.namespace, this.region, message, result);
            }
        });
    }


}
