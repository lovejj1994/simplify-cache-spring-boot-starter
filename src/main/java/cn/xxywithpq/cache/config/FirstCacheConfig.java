package cn.xxywithpq.cache.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class FirstCacheConfig{
    String region;
    String namespace;
    Long size;
    Long expire;
}
