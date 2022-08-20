package com.example.designpatterns.proxy.dynamicproxy;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/3/3 12:14
 */

public class Client {
    public static void main(String[] args) {
        Host host = new Host();
        ProxyInovationHandle pin = new ProxyInovationHandle();
        pin.setRent(host);
        Rent proxy = (Rent)pin.getProxy();
        proxy.rent();
        proxy.sell();
    }
}
