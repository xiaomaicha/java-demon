package com.example.javase.concurrent;

/**
 * @author by dell
 * @Classname Demo1Jmm
 * @Description
 * @Date 2022/8/20 17:24
 */
public class Jmm {
    public static void main(String[] args) throws InterruptedException {
        JmmDemo demo = new JmmDemo();
        Thread t = new Thread(demo);
        t.start();
        Thread.sleep(100);
        demo.flag = false;
        System.out.println("已经修改为false");
        System.out.println(demo.flag);
    }
    static class JmmDemo implements Runnable {
        public volatile boolean flag = true;
        public void run() {
            System.out.println("子线程执行。。。");
            while (flag) {
            }
            System.out.println("子线程结束。。。");
        }
    }
}
