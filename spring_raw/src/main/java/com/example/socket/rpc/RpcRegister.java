package com.example.socket.rpc;

import java.util.HashMap;

/**
 * @author by dell
 * @Classname RpcRegister
 * @Description 服务发现源码
 * @Date 2022/8/21 14:57
 */

public class RpcRegister {
    /**
     * 存储注册的服务提供实现类
     */
    private final HashMap<String, Object> registerMap = new HashMap<>();
    private static final RpcRegister register = new RpcRegister();

    public static RpcRegister buildRegist() {
        return register;
    }

    public RpcRegister regist(String interfaceName, Object obj) {
        registerMap.put(interfaceName, obj);
        return this;
    }

    public Object findServier(String interfaceName) {
        return registerMap.get(interfaceName);
    }
}