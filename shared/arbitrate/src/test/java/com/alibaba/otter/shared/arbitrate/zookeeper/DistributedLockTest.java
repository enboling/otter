package com.alibaba.otter.shared.arbitrate.zookeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.zookeeper.KeeperException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.arbitrate.BaseEventTest;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.lock.DistributedLock;

/**
 * @author jianghang 2011-9-29 下午02:36:19
 * @version 4.0.0
 */
public class DistributedLockTest extends BaseEventTest {

    private final String dir = "/" + getClass().getSimpleName();

    @BeforeClass
    public void init() {
        getZookeeper();
    }

    @Test
    protected void test_lock() {
        ExecutorService exeucotr = Executors.newCachedThreadPool();
        final int count = 50;
        final CountDownLatch latch = new CountDownLatch(count);
        final DistributedLock[] nodes = new DistributedLock[count];
        for (int i = 0; i < count; i++) {
            final DistributedLock node = new DistributedLock(dir);
            nodes[i] = node;
            exeucotr.submit(new Runnable() {

                public void run() {
                    try {
                        node.lock();
                        Thread.sleep(100 + RandomUtils.nextInt(100));
                        System.out.println("id: " + node.getId() + " is leader: " + node.isOwner());
                    } catch (InterruptedException e) {
                        want.fail();
                    } catch (KeeperException e) {
                        want.fail();
                    } finally {
                        latch.countDown();
                        try {
                            node.unlock();
                        } catch (KeeperException e) {
                            want.fail();
                        }
                    }

                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            want.fail();
        }

        exeucotr.shutdown();
    }

    @Test
    protected void test_try_lock() {
        ExecutorService exeucotr = Executors.newCachedThreadPool();
        final int count = 50;
        final CountDownLatch latch = new CountDownLatch(count);

        final DistributedLock[] nodes = new DistributedLock[count];
        for (int i = 0; i < count; i++) {
            final DistributedLock node = new DistributedLock(dir);
            nodes[i] = node;
            exeucotr.submit(new Runnable() {

                public void run() {
                    try {
                        while (node.tryLock() == false) {
                            Thread.sleep(100 + RandomUtils.nextInt(100));
                        }

                        System.out.println("id: " + node.getId() + " is leader: " + node.isOwner());
                    } catch (InterruptedException e) {
                        want.fail();
                    } catch (KeeperException e) {
                        want.fail();
                    } finally {
                        latch.countDown();
                        try {
                            node.unlock();
                        } catch (KeeperException e) {
                            want.fail();
                        }
                    }

                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            want.fail();
        }

        exeucotr.shutdown();
    }
}
