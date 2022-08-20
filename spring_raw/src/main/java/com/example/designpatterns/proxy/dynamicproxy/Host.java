package com.example.designpatterns.proxy.dynamicproxy;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/3/3 12:14
 */

public class Host implements Rent {
    @Override
    public void rent() {
        System.out.println("房屋出租");
    }

    @Override
    public void sell() {
        System.out.println("卖房子");
    }
}
