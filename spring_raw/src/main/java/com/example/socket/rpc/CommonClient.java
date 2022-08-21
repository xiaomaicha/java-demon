package com.example.socket.rpc;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * @author by dell
 * @Classname CommonClient
 * @Description 通用客户端
 * @Date 2022/8/21 14:59
 */

@Data
public class CommonClient {
    private final RpcInitFactory factory;

    public CommonClient(RpcInitFactory factory) {
        this.factory = factory;
    }

    public <T> T invoke(RpcRequest req) {
        RpcResponse response = null;
        req.setRequestId(UUID.randomUUID().toString());
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(factory.getIp(), factory.getPort()));
            ByteBuffer buf1 = ByteBuffer.allocate(2048);
            buf1.put(JSONObject.toJSON(req).toString().getBytes());
            buf1.put(RpcConstant.PROTOCOL_END.getBytes());
            buf1.flip();
            if (buf1.hasRemaining())
                socketChannel.write(buf1);
            buf1.clear();

            ByteBuffer body = ByteBuffer.allocate(2048);
            socketChannel.read(body);
            body.flip();
            if (body.hasRemaining()) {
                response = JSONObject.parseObject(new String(body.array()), RpcResponse.class);
            }
            body.clear();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (T) response;
    }
}