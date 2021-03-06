package live.xsg.cacheoperator;

import live.xsg.cacheoperator.core.AbstractCacheOperator;
import live.xsg.cacheoperator.core.CacheOperator;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.PropertiesResourceLoader;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.JedisTransporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * redis缓存操作器
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheOperator {

    //CacheOperator具体实现
    private InnerRedisCacheOperator innerRedisCacheOperator;

    private RedisCacheOperator(Builder builder) {
        innerRedisCacheOperator = new InnerRedisCacheOperator(builder.transporter, builder.resourceLoader);
    }

    /**
     * 创建代理
     */
    private CacheOperator createProxy() {
        return (CacheOperator) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {CacheOperator.class}, this.innerRedisCacheOperator);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * builder模式
     */
    public static class Builder {
        private Transporter transporter = new JedisTransporter();
        private ResourceLoader resourceLoader = new PropertiesResourceLoader();

        /**
         * 创建对象
         * @return CacheOperator 实现
         */
        public CacheOperator build() {
            RedisCacheOperator redisCacheOperator = new RedisCacheOperator(this);
            return redisCacheOperator.createProxy();
        }

        public Builder transporter(Transporter transporter) {
            this.transporter = transporter;
            return this;
        }

        public Builder resourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
            return this;
        }

    }

    /**
     * 内部类，实现 InvocationHandler，实现代理，控制访问
     */
    private static class InnerRedisCacheOperator extends AbstractCacheOperator implements CacheOperator, InvocationHandler {

        public InnerRedisCacheOperator(Transporter transporter, ResourceLoader resourceLoader) {
            super(transporter, resourceLoader);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //预处理操作
            this.preProcess();

            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if ("toString".equals(methodName) && parameterTypes.length == 0) {
                return this.toString();
            }
            if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return this.hashCode();
            }
            if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return this.equals(args[0]);
            }
            String key = (String) args[0];
            //过滤器链前置处理
            if (!this.filterChain.preFilter(key)) {
                //过滤器链后置处理
                this.filterChain.postFilter(key, null);
                return null;
            }

            Object result = this.failbackCacheOperator.invoke(method, args);

            //过滤器链后置处理
            this.filterChain.postFilter(key, result);

            return result;
        }

        @Override
        public void del(String key) {
            this.stringOperator.del(key);
        }

        @Override
        public String get(String key, long expire, Refresher<String> flusher) {
            return this.stringOperator.get(key, expire, flusher);
        }

        @Override
        public Future<String> getAsync(String key, long expire, Refresher<String> flusher) {
            return this.stringOperator.getAsync(key, expire, flusher);
        }

        @Override
        public Future<String> getAsync(String key, long expire, Refresher<String> flusher, ExecutorService executorService) {
            return this.stringOperator.getAsync(key, expire, flusher, executorService);
        }

        @Override
        public Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.hgetAll(key, expire, flusher);
        }

        @Override
        public Future<Map<String, String>> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.hgetAllAsync(key, expire, flusher);
        }

        @Override
        public Future<Map<String, String>> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService) {
            return this.mapOperator.hgetAllAsync(key, expire, flusher, executorService);
        }

        @Override
        public String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.hget(key, field, expire, flusher);
        }

        @Override
        public Future<Map<String, String>> hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster) {
            return this.mapOperator.hgetAsync(key, field, expire, fluster);
        }

        @Override
        public Future<Map<String, String>> hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster, ExecutorService executorService) {
            return this.mapOperator.hgetAsync(key, field, expire, fluster, executorService);
        }

        @Override
        public List<String> lrange(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
            return this.listOperator.lrange(key, start, end, expire, flusher);
        }

        @Override
        public String lpop(String key, long expire, Refresher<List<String>> flusher) {
            return this.listOperator.lpop(key, expire, flusher);
        }

        @Override
        public String rpop(String key, long expire, Refresher<List<String>> flusher) {
            return this.listOperator.rpop(key, expire, flusher);
        }

        @Override
        public Future<List<String>> lrangeAsync(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
            return this.listOperator.lrangeAsync(key, start, end, expire, flusher);
        }

        @Override
        public Future<List<String>> lrangeAsync(String key, long start, long end, long expire, Refresher<List<String>> flusher, ExecutorService executorService) {
            return this.listOperator.lrangeAsync(key, start, end, expire, flusher, executorService);
        }

        @Override
        public Future<List<String>> lpopAsync(String key, long expire, Refresher<List<String>> flusher) {
            return this.listOperator.lpopAsync(key, expire, flusher);
        }

        @Override
        public Future<List<String>> lpopAsync(String key, long expire, Refresher<List<String>> flusher, ExecutorService executorService) {
            return this.listOperator.lpopAsync(key, expire, flusher, executorService);
        }

        @Override
        public Future<List<String>> rpopAsync(String key, long expire, Refresher<List<String>> flusher) {
            return this.listOperator.rpopAsync(key, expire, flusher);
        }

        @Override
        public Future<List<String>> rpopAsync(String key, long expire, Refresher<List<String>> flusher, ExecutorService executorService) {
            return this.listOperator.rpopAsync(key, expire, flusher, executorService);
        }

        @Override
        public Set<String> smembers(String key, long expire, Refresher<Set<String>> flusher) {
            return this.setOperator.smembers(key, expire, flusher);
        }

        @Override
        public Future<Set<String>> smembersAsync(String key, long expire, Refresher<Set<String>> flusher) {
            return this.setOperator.smembersAsync(key, expire, flusher);
        }

        @Override
        public Future<Set<String>> smembersAsync(String key, long expire, Refresher<Set<String>> flusher, ExecutorService executorService) {
            return this.smembersAsync(key, expire, flusher, executorService);
        }
    }

}
