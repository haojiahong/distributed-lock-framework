package com.hjh.distributed.lock.mysql;

import org.junit.Test;

import java.io.IOException;

/**
 * @author haojiahong created on 2019/12/13
 */
public class MysqlLockTest {

    @Test
    public void test() throws IOException {

        for (int i = 0; i < 2; i++) {
            new Thread(new MyTask()).start();
        }
        System.in.read();
    }

    class MyTask implements Runnable {

        @Override
        public void run() {
            MysqlLock lock = new MysqlLock("aaa");
//            lock.lock();
            if (lock.tryLock()) {
                System.out.println(Thread.currentThread().getName() + "获取到了锁。。。");
                lock.release();
            }else {
                System.out.println(Thread.currentThread().getName() + "未获取到了锁。。。");
            }
        }
    }

}
