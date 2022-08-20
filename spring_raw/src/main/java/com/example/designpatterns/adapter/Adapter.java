package com.example.designpatterns.adapter;

/**
 * @author dell
 * @Classname Adapter
 * @Description 转换器实现，需要连接USB，连接网线
 * @Date 2021/4/11 20:11
 * @Created by dell
 */

//1.继承
//2.组合
public class Adapter  implements NetToUsb {

    Adaptee adaptee;

    public Adapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void handleRequest() {
        adaptee.request();//可以上网了
    }
}
