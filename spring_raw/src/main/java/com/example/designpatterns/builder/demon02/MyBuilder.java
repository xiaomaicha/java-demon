package com.example.designpatterns.builder.demon02;

/**
 * @author dell
 * @Classname Builder
 * @Description TODO
 * @Date 2021/4/11 14:00
 * @Created by dell
 */
public abstract class MyBuilder {

    abstract MyBuilder buildA(String msg);
    abstract MyBuilder buildB(String msg);
    abstract MyBuilder buildC(String msg);
    abstract MyBuilder buildD(String msg);

    abstract Product getProduct();

}
