package live.xsg.cacheoperator;

import live.xsg.cacheoperator.flusher.Refresher;

import java.util.concurrent.Executor;

/**
 * 缓存操作器
 * Created by xsg on 2020/7/20.
 */
public interface CacheOperator {

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 ""
     * 2.缓存存在数据，则返回缓存中的旧数据
     *
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String getString(String key, long expire, Refresher<String> flusher);

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 如果当前没有其他线程在刷新缓存，则开启一个线程执行缓存刷新，当前线程返回""或者缓存中的旧数据
     * 如果当前已经有其他现在在刷新缓存，则当前线程返回""或者缓存中的旧数据
     * 使用 Executor executor = Executors.newCachedThreadPool()
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String getStringAsync(String key, long expire, Refresher<String> flusher);

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 如果当前没有其他线程在刷新缓存，则开启一个线程执行缓存刷新，当前线程返回""或者缓存中的旧数据
     * 如果当前已经有其他现在在刷新缓存，则当前线程返回""或者缓存中的旧数据
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @param executor 指定以线程池实现
     * @return 返回缓存中的数据
     */
    String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor);

    /**
     * 从缓存中获取数据，字符串类型，如果缓存中无数据或者缓存过期，则返回 Constants.EMPTY_STRING
     * @param key key
     * @return 返回缓存数据，如果缓存中无数据或者缓存过期，则返回 Constants.EMPTY_STRING
     */
    String getString(String key);
}
