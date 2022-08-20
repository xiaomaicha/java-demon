package com.example.javase.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CountdownLatchExample {

    public static void main(String[] args) throws InterruptedException {
//        Timer timer = new Timer();// 实例化Timer类
//        timer.schedule(new TimerTask() {
//            public void run() {
//                System.out.println("退出");
//                this.cancel();
//            }
//        }, 5000);// 这里百毫秒
//        System.out.println("本程序存在5秒后自动退出");

//        Thread.currentThread();
//        Thread.sleep(2333);        //延迟2333毫秒
//        System.out.println("延时2333毫秒");

        final int totalThread = 10;
        CountDownLatch countDownLatch = new CountDownLatch(totalThread);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                System.out.print("run..");
                countDownLatch.countDown();
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);//MILLISECONDS表示以毫秒为单位延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        countDownLatch.await();
        System.out.println("end");
        executorService.shutdown();
    }
}

