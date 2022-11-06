package com.example.filequeue;


import com.example.filequeue.exception.QueueException;
import com.example.filequeue.queue.PermanentQueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:test
 *
 * @author wxy
 * create 2017-05-14 下午10:31
 */

public class QueueTest {
    private String queueName;
    private int poolSize;
    private int dataNums;

    public static void main(String[] args) {
        /**读写性能测试*/
        QueueTest queueTest = new QueueTest("0923_8", 1, 2100000);
//        queueTest.writePerformanceTest();
        queueTest.readPerformanceTest();

//        QueueTest queueTest = new QueueTest();
//        try {
//            //写测试
//            queueTest.testOffer();
//            //take测试
//            queueTest.testTake();
//            //pop测试
//            queueTest.testPop();
//        } catch (QueueException e) {
//            e.printStackTrace();
//        }

    }

    /**
     * 写性能测试
     */
    private void writePerformanceTest() {
        ExecutorService productPool = Executors.newFixedThreadPool(poolSize);
        final CountDownLatch countDownLatch = new CountDownLatch(poolSize);
        final AtomicInteger allSum = new AtomicInteger();
        final long start = System.currentTimeMillis();
        for (int i = 0; i < poolSize; i++) {
            productPool.submit(() -> {
                int sum = 0;
                for (int j = 0; j < dataNums; j++) {
                    try {
                        PermanentQueue.offer(queueName, String.valueOf(j));
                    } catch (QueueException e) {
                        e.printStackTrace();
                    }
                    sum = sum + j;
                }

                allSum.addAndGet(sum);
                System.out.println(
                        "Thread: " + Thread.currentThread().getName() + " 100000 data write take == " + (
                                System.currentTimeMillis() - start));
                countDownLatch.countDown();

            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Write take time == " + (System.currentTimeMillis() - start));
        System.out.println("Write sum == " + allSum.get());
    }

    /**
     * 写性能测试
     */
    private void readPerformanceTest() {
        ExecutorService consumersPool = Executors.newFixedThreadPool(poolSize);
        final CountDownLatch countDownLatch = new CountDownLatch(poolSize);
        final AtomicInteger allSum = new AtomicInteger();
        final Long start = System.currentTimeMillis();
        for (int i = 0; i < poolSize; i++) {
            consumersPool.submit(new Runnable() {
                @Override
                public void run() {
                    int sum = 0;
                    int count = 0;
                    for (int j = 0; j < dataNums; j++) {
                        String value = null;
                        try {
                            value = PermanentQueue.take(queueName);
                        } catch (QueueException e) {
                            e.printStackTrace();
                        }
                        if (null != value && !"".equals(value)) {
                            count++;
                            sum = sum + Integer.parseInt(value);
                        }

                    }

                    countDownLatch.countDown();
                    allSum.addAndGet(sum);
                    System.out.println(
                            "Thread: " + Thread.currentThread().getName() + " sum == " + sum + " count == " + count
                                    + " take time == " + (System.currentTimeMillis() - start));
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Read take time == " + (System.currentTimeMillis() - start));
        System.out.println("Read sum == " + allSum);
    }

    public void testOffer() throws QueueException {
        PermanentQueue.offer("category", "data1");
        System.out.println("Offer data1 to category");
    }

    public void testPop() throws QueueException {
        String data = PermanentQueue.pop("category");
        System.out.println("Pop data from category. data == " + data);

    }

    public void testTake() throws QueueException {
        String data = PermanentQueue.take("category");
        System.out.println("Take data from category. data == " + data);
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getDataNums() {
        return dataNums;
    }

    public void setDataNums(int dataNums) {
        this.dataNums = dataNums;
    }

    public QueueTest(String queueName, int poolSize, int dataNums) {
        this.queueName = queueName;
        this.poolSize = poolSize;
        this.dataNums = dataNums;
    }

    public QueueTest() {
    }
}
