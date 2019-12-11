package com.hjh.distributed.lock.zookeeper;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author haojiahong created on 2019/12/11
 */
public class ZookeeperLock {

    private static ZkClient zkClient;
    private static final String PARENT_LOCK_PATH_ROOT = "/distribute_lock";

    private String name;
    private String PARENT_LOCK_PATH;

    static {
        zkClient = new ZkClient("127.0.0.1:2181");
    }

    public ZookeeperLock(String name) {
        this.name = name;
        this.PARENT_LOCK_PATH = PARENT_LOCK_PATH_ROOT + "/" + name;
    }

    private String currentLockPath;
    private CountDownLatch countDownLatch;

    public void lock() {
        if (!zkClient.exists(PARENT_LOCK_PATH)) {
            try {
                zkClient.createPersistent(PARENT_LOCK_PATH);
            } catch (Exception ignored) {

            }
        }
        currentLockPath = zkClient.createEphemeralSequential(PARENT_LOCK_PATH + "/", System.currentTimeMillis());
        System.out.println(Thread.currentThread().getName() + "currentLockPath=" + currentLockPath);
        checkMinNode(currentLockPath);

    }

    //解锁
    public void unlock() {
        System.out.println("delete : " + currentLockPath);
        zkClient.delete(currentLockPath);
    }

    private boolean checkMinNode(String currentLockPath) {
        System.out.println("checkMinNode" + currentLockPath);
        List<String> children = zkClient.getChildren(PARENT_LOCK_PATH);
        Collections.sort(children);
        int index = children.indexOf(currentLockPath.substring(PARENT_LOCK_PATH.length() + 1));
        if (index == 0) {
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
            return true;
        } else {
            String waitPath = PARENT_LOCK_PATH + "/" + children.get(index - 1);
            waitForLock(waitPath, currentLockPath);
            return false;
        }


    }

    private void waitForLock(String waitPath, String currentLockPath) {
        countDownLatch = new CountDownLatch(1);
        zkClient.subscribeDataChanges(waitPath, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                checkMinNode(currentLockPath);
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countDownLatch = null;

    }

}
