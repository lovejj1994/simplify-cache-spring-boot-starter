package cn.xxywithpq.cache;


import cn.xxywithpq.cache.listener.CacheListener;

/**
 * 二级缓存行为接口
 */
public interface SecondCache extends Cache, CacheListener {


    /**
     * 同步数据到一级cache -- 新增行为(JSON传递)
     *
     * @return
     */
    void syncPutFirstCacheJson(String region, String key, String valueStr);

    /**
     * 同步数据到一级cache -- 删除行为
     *
     * @return
     */
    void syncDeleteFirstCache(String region, String key);


}