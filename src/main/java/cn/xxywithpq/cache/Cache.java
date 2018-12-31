package cn.xxywithpq.cache;

/**
 * 缓存接口
 * 定义基本缓存行为
 *
 * @author qian.pan
 */
public interface Cache {

    /**
     * 存(会覆盖)
     *
     * @param key
     * @param value
     */
    Object put(String key, String value);

    /**
     * 删
     *
     * @param key
     */
    Object delete(String key);

    /**
     * get
     *
     * @param key
     */
    Object get(String key);

    /**
     * 封装key
     *
     * @param key
     * @return
     */
    String key(String key);
}