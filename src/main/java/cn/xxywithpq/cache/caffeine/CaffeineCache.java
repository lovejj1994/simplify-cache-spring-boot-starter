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
package cn.xxywithpq.cache.caffeine;

import cn.xxywithpq.cache.FirstCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache
 *
 * @author Winter Lau(javayou@gmail.com)
 */
@Data
public class CaffeineCache implements FirstCache {

    private com.github.benmanes.caffeine.cache.Cache<String, String> cache;
    private Long size;
    private Long expire;
    private String region;
    private String namespace;

    public CaffeineCache(Long size, Long expire, String namespace, String region) {
        if (null == size) {
            size = 1000L;
        }
        this.size = size;
        this.expire = expire;
        this.namespace = namespace;
        this.region = region;
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .maximumSize(size);
        if (null != expire) {
            caffeine.expireAfterWrite(expire, TimeUnit.MILLISECONDS);
        }
        cache = caffeine.build();
    }

    @Override
    public Object put(String key, String value) {
        cache.put(key(key), value);
        return null;
    }

    @Override
    public Object delete(String key) {
        cache.invalidate(key(key));
        return null;
    }

    @Override
    public Object get(String key) {
        return cache.getIfPresent(key(key));
    }

    @Override
    public String key(String key) {
        return this.namespace + ":" + this.region + ":" + key;
    }

}
