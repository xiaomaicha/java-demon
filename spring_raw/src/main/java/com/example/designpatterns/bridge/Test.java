package com.example.designpatterns.bridge;

/**
 * @author by dell
 * @Classname Test
 * @Description TODO
 * @Date 2021/4/11 20:57
 */
public class Test {
    public static void main(String[] args) {
        //苹果笔记本
        Computer computer = new Laptop(new Apple());
        computer.info();

        //联想台式机
        Computer computer1 = new Desktop(new Lenovo());
        computer1.info();

    }
}
