package com.example.designpatterns.builder.demon01;

/**
 * @author dell
 * @Classname Builder
 * @Description TODO
 * @Date 2021/4/11 14:00
 * @Created by dell
 */
public abstract class MyBuilder {
    abstract void buildA();
    abstract void buildB();
    abstract void buildC();
    abstract void buildD();

    abstract Product getProduct();
}
