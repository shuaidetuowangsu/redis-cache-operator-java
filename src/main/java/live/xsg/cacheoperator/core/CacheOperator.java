package live.xsg.cacheoperator.core;

/**
 * 缓存操作器
 * Created by xsg on 2020/7/20.
 */
public interface CacheOperator extends StringOperator, MapOperator, ListOperator, SetOperator {

    /**
     * 删除key
     * @param key key
     */
    void del(String key);

}
