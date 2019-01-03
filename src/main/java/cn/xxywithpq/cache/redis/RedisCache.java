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

import cn.xxywithpq.cache.SecondCache;
import cn.xxywithpq.cache.constants.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static cn.xxywithpq.cache.constants.CommonConstants.REDIS_TOPIC_DELETE;
import static cn.xxywithpq.cache.constants.CommonConstants.REDIS_TOPIC_PUT;

/**
 * Caffeine cache
 *
 * @author Winter Lau(javayou@gmail.com)
 */
@Data
@Slf4j
public class RedisCache implements SecondCache {

    private RedissonClient redissonClient;
    private RedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private Long expire;
    private String region;
    private String namespace;

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
    }

    @Override
    public Object put(String key, String value) {
        if (null != expire) {
            this.valueOperations.set(key(key), value, expire, TimeUnit.MILLISECONDS);
        } else {
            this.valueOperations.set(key(key), value);
        }
        return null;
    }

    @Override
    public Object delete(String key) {
        Boolean delete = this.redisTemplate.delete(key(key));
        return delete;
    }

    @Override
    public Object get(String key) {
        String value = this.valueOperations.get(key(key));
        return value;
    }

    @Override
    public void syncPutFirstCacheJson(String region, String key, String valueStr) {
        JSONObject message = new JSONObject(2);
        message.put(CommonConstants.JSON_KEY, key);
        message.put(CommonConstants.JSON_VALUE, valueStr);
        redisTemplate.convertAndSend(REDIS_TOPIC_PUT + this.namespace + ":" + region, message.toJSONString());
        log.info("syncFirstCacheJson, namespace:{} region:{} key: {} value: {}", this.namespace, region, key, valueStr);
    }

    @Override
    public void syncDeleteFirstCache(String region, String key) {
        redisTemplate.convertAndSend(REDIS_TOPIC_DELETE + this.namespace + ":" + region, key(key));
        log.info("syncDeleteFirstCache,namespace:{} region:{} key:{}", this.namespace, region, key);
    }

    @Override
    public String key(String key) {
        return this.namespace + ":" + this.region + ":" + key;
    }


}
