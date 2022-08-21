package com.example.socket.rpc;

/**
 * @author by dell
 * @Classname DemoImp
 * @Description
 * @Date 2022/8/21 15:07
 */

public class ServerImp implements Idemo {
    @Override
    public Integer add(Integer i, Integer j) {
        return i + j;
    }

    public static void main(String[] args) {
        ProviderServer server = new ProviderServer(8090);
        RpcRegister.buildRegist().regist(Idemo.class.getName(), new ServerImp());
        new Thread(server).start();
    }
}