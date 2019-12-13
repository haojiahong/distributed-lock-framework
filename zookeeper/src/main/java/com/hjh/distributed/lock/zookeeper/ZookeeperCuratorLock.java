package com.hjh.distributed.lock.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author haojiahong created on 2019/12/13
 */
public class ZookeeperCuratorLock {
    private static final String PARENT_LOCK_PATH_ROOT = "distribute_lock";
    private static CuratorFramework client;

    static {
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .namespace(PARENT_LOCK_PATH_ROOT)
                .build();
        client.start();
    }

    /**
     * 锁定的资源
     */
    private String resource;
    private String lockPath;
    private String lockPathPrefix;
    private CountDownLatch countDownLatch;

    public ZookeeperCuratorLock(String resource) {
        this.resource = "/" + resource;
        this.lockPathPrefix = this.resource + "/lock-";
    }

    public void lock() {
        try {
            lockPath = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(this.lockPathPrefix);
            System.out.println("lock.lockPath=" + lockPath);
            checkMinNode(lockPath);
            System.out.println(Thread.currentThread().getName() + "加锁。。。");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private void checkMinNode(String lockPath) throws Exception {
        List<String> childrenList = client.getChildren().forPath(this.resource);
        Collections.sort(childrenList);
        int index = childrenList.indexOf(lockPath.substring(this.resource.length() + 1));
        //获取锁
        if (index == 0) {
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }
        //有序节点值非最小，未获取锁，监听它的前一节点
        else {
            String waitPath = this.resource + "/" + childrenList.get(index - 1);
            waitForLock(waitPath);
        }
    }

    private void waitForLock(String waitPath) throws Exception {
        try {
            //这种注册监听只会触发一次，不同于cache。
            client.getData().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    //这一步watcher会在另外的线程执行了
                    checkMinNode(lockPath);
                }
            }).forPath(waitPath);
        } catch (Exception e) {
            //如果监听的节点设置监听前已经删除了，则重新判断是否最小节点，是否可以获取锁
            if (e instanceof KeeperException.NoNodeException) {
                this.checkMinNode(lockPath);
                return;
            }
        }
        this.countDownLatch = new CountDownLatch(1);
        this.countDownLatch.await();
    }

    public void release() {
        try {
            client.delete().forPath(this.lockPath);
            System.out.println(Thread.currentThread().getName() + "解锁。。。");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
