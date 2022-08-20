package com.example.designpatterns.bridge;

/**
 * @author by dell
 * @Classname Apple
 * @Description TODO
 * @Date 2021/4/11 20:50
 */
public class Apple implements Brand {
    @Override
    public void info() {
        System.out.println("苹果");
    }
}
