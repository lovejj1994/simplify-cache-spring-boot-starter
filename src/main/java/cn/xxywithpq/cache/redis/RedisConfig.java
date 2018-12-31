package cn.xxywithpq.cache.redis;

import cn.xxywithpq.cache.config.SecondCacheConfig;
import lombok.Data;
import lombok.ToString;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.data.redis.core.RedisTemplate;

@Data
@ToString(callSuper = true)
public class RedisConfig extends SecondCacheConfig {
    RedisTemplate redisTemplate;
    RedissonClient redissonClient;
}
