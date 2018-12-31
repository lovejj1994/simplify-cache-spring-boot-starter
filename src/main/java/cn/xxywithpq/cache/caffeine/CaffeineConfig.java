package cn.xxywithpq.cache.caffeine;

import cn.xxywithpq.cache.config.FirstCacheConfig;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class CaffeineConfig extends FirstCacheConfig {
    Long size;
    Long expire;
}
