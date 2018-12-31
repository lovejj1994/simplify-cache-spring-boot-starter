package cn.xxywithpq.cache.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class SecondCacheConfig {
    String region;
    String namespace;
    Long expire;
}
