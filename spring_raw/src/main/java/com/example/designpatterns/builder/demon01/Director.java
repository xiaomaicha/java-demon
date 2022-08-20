package com.example.designpatterns.builder.demon01;

/**
 * @author dell
 * @Classname Director
 * @Description 指挥工程如何构建
 * @Date 2021/4/11 14:31
 * @Created by dell
 */
public class Director {
    public Product build(MyBuilder myBuilder){
        myBuilder.buildA();
        myBuilder.buildB();
        myBuilder.buildC();
        myBuilder.buildD();

        return myBuilder.getProduct();
    }
}
