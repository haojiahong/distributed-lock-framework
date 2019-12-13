package com.hjh.distributed.lock.zookeeper;

import org.junit.Test;

import java.io.IOException;

/**
 * @author haojiahong created on 2019/12/13
 */
public class ZookeeperCuratorLockTest {
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
            ZookeeperCuratorLock lock = new ZookeeperCuratorLock("curatorLock");
            lock.lock();

            System.out.println(Thread.currentThread().getName() + "获取到了锁。。。");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.release();
            System.out.println("===========");
        }
    }
}
