package com.example.designpatterns.adapter;

/**
 * @Classname Adapter
 * @Description 网线，要被适配的类
 * @Date 2021/4/11 20:08
 * @Created by dell
 */
public class Adaptee {
    public void request(){
        System.out.println("上网");
    }
}
