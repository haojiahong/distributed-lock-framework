package com.hjh.distributed.lock.zookeeper;

import org.junit.Test;

import java.io.IOException;

/**
 * @author haojiahong created on 2019/12/11
 */
public class ZookeeperLockTest {


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
            ZookeeperLock zookeeperLock = new ZookeeperLock("test");
            zookeeperLock.lock();
            System.out.println(Thread.currentThread().getName() + "获取到了锁。。。");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            zookeeperLock.unlock();


        }
    }
}
