package com.example.designpatterns.proxy.staticproxy;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/3/3 12:14
 */

public class Proxy implements Rent{
    private Host host;

    public void setHost(Host host) {
        this.host = host;
    }

    public Proxy(Host host) {
        this.host = host;
    }

    public void rent(){
        seeHouese();
        host.rent();
        fare();
    }

    //看房
    public void seeHouese(){
        System.out.println("带房客看房");
    }

    //收中介费
    public void fare(){
        System.out.println("收中介费");
    }
}
