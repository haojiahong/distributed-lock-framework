package com.hjh.distributed.lock.zookeeper;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;
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
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            zookeeperLock.unlock();


        }
    }

    @Test
    public void testListener() throws IOException {
        ZkClient zkClient = new ZkClient("127.0.0.1:2181");
        zkClient.subscribeDataChanges("/aaabbb", new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                System.out.println("datachange");
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                System.out.println("监听生效了");
            }
        });

        System.in.read();
    }

    /**
     * 监听的节点如果不存在的话，会抛出org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for /aaabbb异常
     * curator分布式锁中的实现是，根据抛出的异常，进行重试监听。
     */
    @Test
    public void testCuratorLister() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        try {
            curatorFramework.getData().usingWatcher((Watcher) event -> {
                System.out.println("aaaabbbbb");
            }).forPath("/aaabbb");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCurator() throws IOException {
        for (int i = 0; i < 10; i++) {
            new Thread(new MyCuratorTask()).start();
        }
        System.in.read();

    }

    class MyCuratorTask implements Runnable {

        @Override
        public void run() {
            try {

                CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
                curatorFramework.start();
                InterProcessMutex mutex = new InterProcessMutex(curatorFramework, "/abbb");
                mutex.acquire();
                System.out.println(Thread.currentThread().getName() + "获取到了锁。。。");
                mutex.release();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
