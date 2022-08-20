package com.example.designpatterns.builder.demon02;

/**
 * @Classname Test
 * @Description TODO
 * @Date 2021/4/11 15:02
 * @Created by dell
 */
public class Test {
    public static void main(String[] args) {
        Worker worker = new Worker();
        Product product = worker.buildA("全家桶").getProduct();
        System.out.println(product.toString());
    }
}
