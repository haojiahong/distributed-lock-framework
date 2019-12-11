package com.hjh.distributed.lock.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collections;

/**
 * @author haojiahong created on 2019/12/11
 */
public class RedisLock {
    private static JedisPool jedisPool;

    static {
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
    }

    public Jedis getConnection() {
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

    public boolean lock(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = this.getConnection();
            String result = jedis.set(key, value, "NX", "PX", 60 * 1000L);
            if ("OK".equals(result)) {
                return true;
            }
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public boolean release(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = this.getConnection();
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(value));
            if (Long.valueOf(1L).equals(result)) {
                return true;
            }
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
