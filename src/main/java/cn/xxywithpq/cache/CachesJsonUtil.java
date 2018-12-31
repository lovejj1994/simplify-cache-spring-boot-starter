package cn.xxywithpq.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
@Slf4j
public class CachesJsonUtil {

    private CacheProviderHolder cacheProviderHolder;

    private String namespace;

    public CachesJsonUtil() {
    }


    public <T> T get(String region, String key, Class<T> clazz) {
        Object obj;
        try {
            if (StringUtils.isBlank(key) || StringUtils.isBlank(region)) {
                return null;
            }
            obj = cacheProviderHolder.getFirstProvider(name(namespace, region)).get(key);
            if (!Objects.isNull(obj)) {
                //一级缓存命中
                log.info("一级缓存命中 region {} key {} value {}", region, key, obj);
                String json = (String) obj;
                if (StringUtils.isNotBlank(json)) {
                    return JSON.parseObject(json, clazz);
                }
                //从二级取缓存
            } else {
                obj = cacheProviderHolder.getSecondProvider(name(namespace, region)).get(key);
                if (!Objects.isNull(obj)) {
                    log.info("二级缓存命中 region {} key {} value {}", region, key, obj);
                    String json = (String) obj;
                    cacheProviderHolder.getSecondProvider(name(namespace, region)).syncPutFirstCacheJson(region, key, json);
                    return JSON.parseObject(json, clazz);
                }
            }

        } catch (Exception e) {
            log.warn("CachesJsonUtil region {} key {} clazz{} get e={}", region, key, clazz, e);
        }
        return null;
    }

    public void delete(String region, String key) {
        try {
            if (StringUtils.isBlank(key) || StringUtils.isBlank(region)) {
                return;
            }
            cacheProviderHolder.getSecondProvider(name(this.namespace, region)).delete(key);
            cacheProviderHolder.getSecondProvider(name(this.namespace, region)).syncDeleteFirstCache(region, key);
        } catch (Exception e) {
            log.warn("SecondLevelCacheUtil delete e={}", e);
        }
    }

    public void set(String region, String key, Object value) {
        try {
            if (StringUtils.isNotBlank(key) && !Objects.isNull(value)) {
                String valueStr = JSONObject.toJSONString(value);
                cacheProviderHolder.getSecondProvider(name(this.namespace, region)).put(key, valueStr);
                cacheProviderHolder.getSecondProvider(name(this.namespace, region)).syncPutFirstCacheJson(region, key, valueStr);
            }
        } catch (Exception e) {
            log.warn("SecondLevelCacheUtil set e={}", e);
        }
    }

    public void setIfAbsent(String region, String key, Object value, Class clazz) {
        if (null == get(region, key, clazz)) {
            set(region, key, value);
        }
    }


    private String name(String namespace, String region) {
        return namespace + ":" + region;
    }


    void setCacheProviderHolder(CacheProviderHolder cacheProviderHolder) {
        this.cacheProviderHolder = cacheProviderHolder;
    }

    void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    @Override
    public String toString() {
        return "CachesJsonUtil{" +
                "cacheProviderHolder=" + cacheProviderHolder +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
