package com.example.socket.rpc;

/**
 * @author by dell
 * @Classname RpcInitFactory
 * @Description 初始化工厂类
 * @Date 2022/8/21 14:59
 */

public class RpcInitFactory {
    /**
     * 客户端连接远程ip地址
     **/
    private String ip;
    /***远程端口*/
    private int port;


    public RpcInitFactory(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}