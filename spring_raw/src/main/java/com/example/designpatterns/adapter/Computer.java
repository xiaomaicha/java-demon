package com.example.designpatterns.adapter;

/**
 * @Classname Computer
 * @Description 电脑，想上网，但插不上网线
 * @Date 2021/4/11 20:09
 * @Created by dell
 */
public class Computer {
    //电脑需要连接上转接器，才可以上网
    public void net(NetToUsb adapter){
        adapter.handleRequest();
    }

    public static void main(String[] args) {
        //电脑，适配器，网线
        Computer computer = new Computer();
        //电脑
        Adaptee adaptee = new Adaptee();
        //网线
        Adapter adapter = new Adapter(adaptee);
        //转换器

        computer.net(adapter);
    }
}
