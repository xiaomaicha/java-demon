package com.example.javase.concurrent;

//import cn.hutool.core.date.DateUtil;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/2/5 15:35
 */
public class SynchronizedDemo1 implements Runnable {
    /**
     * 全局变量
     * 创建一个计数器
     */
    private static int counter = 1;

    @Override
    public void run() {
//        Date startDate = DateUtil.date();
        synchronized (this) {
            for (int i = 0; i < 5; i++) {
                try {
                    System.out.println("线程 ：" + Thread.currentThread().getName() + " 当前计数器 ：" + (counter++));
//                    System.out.println("开始时间 ：" + startDate + " 当前时间 ：" + DateUtil.date());
                    System.out.println();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SynchronizedDemo1 syncThread1 = new SynchronizedDemo1();
        SynchronizedDemo1 syncThread2 = new SynchronizedDemo1();
        Thread thread1 = new Thread(syncThread1, "sync-thread-1");
        Thread thread2 = new Thread(syncThread2, "sync-thread-2");
        thread1.start();
        thread2.start();
    }


//    public static void main(String[] args) {
//        SynchronizedDemo1 syncThread = new SynchronizedDemo1();
//        Thread thread1 = new Thread(syncThread, "sync-thread-1");
//        Thread thread2 = new Thread(syncThread, "sync-thread-2");
//        thread1.start();
//        thread2.start();
//    }
}

