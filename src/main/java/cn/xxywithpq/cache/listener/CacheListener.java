package cn.xxywithpq.cache.listener;

import cn.xxywithpq.cache.FirstCache;

/**
 * 两级的缓存管理器
 */
public interface CacheListener {

    /**
     * 定义清除一级缓存监听
     *
     * @param firstCache
     */
    void initClearFirstCacheListener(FirstCache firstCache);

    /**
     * 定义添加一级缓存监听
     *
     * @param firstCache
     */
    void initPutFirstCacheListener(FirstCache firstCache);
}
