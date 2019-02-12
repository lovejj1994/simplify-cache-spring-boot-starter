package cn.xxywithpq.cache;


import java.util.Date;

/**
 * 二级缓存行为接口
 */
public interface SecondCache extends Cache {


    /**
     * 同步数据到一级cache -- 新增行为(JSON传递)
     *
     * @return
     */
    void syncPutFirstCacheJson(String key, String valueStr);

    /**
     * 同步数据到一级cache -- 删除行为
     *
     * @return
     */
    void syncDeleteFirstCache(String key);

    /**
     * 重置ttl时间
     *
     * @return
     */
    Boolean resetTtl(String key, Date date);
}