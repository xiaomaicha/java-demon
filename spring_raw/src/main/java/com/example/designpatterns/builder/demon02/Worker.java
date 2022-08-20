package com.example.designpatterns.builder.demon02;

/**
 * @Classname Worker
 * @Description TODO
 * @Date 2021/4/11 14:57
 * @Created by dell
 */
public class Worker extends MyBuilder {

    Product product;

    public Worker() {
        this.product = new Product();
    }

    @Override
    MyBuilder buildA(String msg) {
        product.setBuildA(msg);
        System.out.println(msg);
        return this;
    }

    @Override
    MyBuilder buildB(String msg) {
        product.setBuildB(msg);
        System.out.println(msg);
        return this;
    }

    @Override
    MyBuilder buildC(String msg) {
        product.setBuildC(msg);
        System.out.println(msg);
        return this;
    }

    @Override
    MyBuilder buildD(String msg) {
        product.setBuildD(msg);
        System.out.println(msg);
        return this;
    }

    @Override
    Product getProduct() {
        return product;
    }
}
