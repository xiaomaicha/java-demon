package com.example.designpatterns.proxy.staticproxy;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/3/3 12:14
 */

public class Client {
    public static void main(String[] args) {
        Host host = new Host();
        Proxy proxy = new Proxy(host);
        proxy.rent();
    }
}
