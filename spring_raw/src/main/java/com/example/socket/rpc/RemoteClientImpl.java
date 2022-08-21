package com.example.socket.rpc;

/**
 * @author by dell
 * @Classname DemoRemoteImpl
 * @Description
 * @Date 2022/8/21 15:07
 */
public class RemoteClientImpl implements Idemo {


    private CommonClient client;

    public RemoteClientImpl(CommonClient client) {
        this.client = client;
    }

    @Override
    public Integer add(Integer i, Integer j) {
        //构造rpc请求实体类
        RpcRequest rpcRequest = new RpcRequest();
        //设置版本号
        rpcRequest.setServiceVersion("123");
        //设置调用的接口名称
        rpcRequest.setInterfaceName(Idemo.class.getName());
        //设置调用方法名称
        rpcRequest.setMethodName("add");
        //设置参数
        rpcRequest.setParameters(new Integer[]{i, j});
        //设置参数类型
        rpcRequest.setParameterTypes(new Class[]{Integer.class, Integer.class});
        //进行远程调用
        RpcResponse response = client.invoke(rpcRequest);
        if (null != response) {
            return Integer.parseInt(response.getResult().toString());
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        RpcInitFactory initFactory = new RpcInitFactory("127.0.0.1", 8090);
        Idemo demo = new RemoteClientImpl(new CommonClient(initFactory));
        for (int i = 0; i < 200; i++) {
            System.out.println(i);
            new Thread(() -> {
                System.out.println(demo.add(2, 1));
            }).start();
        }
        Thread.sleep(1000);
    }
}