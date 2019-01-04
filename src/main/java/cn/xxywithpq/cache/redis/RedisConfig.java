package cn.xxywithpq.cache.redis;

import cn.xxywithpq.cache.config.SecondCacheConfig;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.redis.core.RedisTemplate;

@Data
@ToString(callSuper = true)
public class RedisConfig extends SecondCacheConfig {
    RedisTemplate redisTemplate;
}
