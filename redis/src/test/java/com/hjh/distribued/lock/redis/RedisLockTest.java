package com.hjh.distribued.lock.redis;

import com.hjh.distributed.lock.redis.RedisLock;
import org.junit.Test;

import java.io.IOException;

/**
 * @author haojiahong created on 2019/12/11
 */
public class RedisLockTest {

    private RedisLock redisLock = new RedisLock();

    @Test
    public void test() throws IOException {
        for (int i = 0; i < 10; i++) {
            new Thread(new MyTask()).start();
        }

        System.in.read();

    }

    class MyTask implements Runnable {

        @Override
        public void run() {
            if (redisLock.lock("key", "value")) {
                try {
                    System.out.println(Thread.currentThread().getName() + "获取到了锁。。。");
                    Thread.sleep(5 * 1000L);
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(Thread.currentThread().getName() + "释放了锁。。。");
                    redisLock.release("key", "value");
                }
            }
            System.out.println(Thread.currentThread().getName() + "获取锁失败。。。");
        }
    }

}
