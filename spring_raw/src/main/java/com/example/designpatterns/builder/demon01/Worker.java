package com.example.designpatterns.builder.demon01;

/**
 * @author dell
 * @Classname Worker
 * @Description TODO
 * @Date 2021/4/11 14:15
 * @Created by dell
 */
public class Worker extends MyBuilder {

    Product product;

    public Worker() {
        this.product = new Product();
    }

    @Override
    void buildA() {
        product.setBuildA("地基");
        System.out.println("地基");
    }

    @Override
    void buildB() {
        product.setBuildB("钢筋");
        System.out.println("钢筋");
    }

    @Override
    void buildC() {
        product.setBuildC("输电线");
        System.out.println("输电线");
    }

    @Override
    void buildD() {
        product.setBuildD("粉刷");
        System.out.println("粉刷");
    }

    @Override
    Product getProduct() {
        return product;
    }
}
