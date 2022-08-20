package com.example.javase.concurrent;

class A extends Thread {
    @Override
    public void run() {
        System.out.println("A");
    }
}

class B extends Thread {

    private A a;

    B(A a) {
        this.a = a;
    }

    @Override
    public void run() {
        try {
            a.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("B");
    }
}


public class JoinExample {

    public void test() {
        A a = new A();
        B b = new B(a);
        b.start();
        a.start();
    }

    public static void main(String[] args) {
        JoinExample example = new JoinExample();
        example.test();
    }
}
