package com.example.socket.rpc;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author by dell
 * @Classname ProviderServer
 * @Description 服务提供者源码
 * @Date 2022/8/21 14:58
 */

public class ProviderServer implements Runnable {
    /**
     * 服务提供端口
     */
    private int port;


    public ProviderServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyChannels = selector.selectNow();
                if (readyChannels == 0) continue;
                Set selectedKeys = selector.selectedKeys();
                Iterator keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = (SelectionKey) keyIterator.next();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel1.accept();
                        ByteBuffer buf1 = ByteBuffer.allocate(2048);
                        socketChannel.read(buf1);
                        buf1.flip();
                        String receiveStr = new String(buf1.array());
                        if (buf1.hasRemaining()) {
                            System.out.println(">>>服务端收到数据：" + receiveStr);
                            //判断接受的内容是否有结束符，如果有，说明是一个请求结束。
                            if (receiveStr.contains(RpcConstant.PROTOCOL_END)) {
                                RpcRequest req = JSONObject.parseObject(receiveStr.replace(RpcConstant.PROTOCOL_END, ""), RpcRequest.class);
                                RpcResponse res = new RpcResponse();
                                res.setRequestId(req.getRequestId());
                                System.out.println(req.toString());
                                Class<?> remoteInterface = Class.forName(req.getInterfaceName());
                                Method method = remoteInterface.getMethod(req.getMethodName(), req.getParameterTypes());
                                Object obj = method.invoke(RpcRegister.buildRegist().findServier(req.getInterfaceName()), req.getParameters());
                                res.setException(null);
                                res.setResult(obj);
                                buf1.clear();
                                buf1.put(JSONObject.toJSON(res).toString().getBytes());
                                buf1.flip();
                                socketChannel.write(buf1);
                            }
                        }
                        socketChannel.close();
                    } else if (key.isConnectable()) {
                    } else if (key.isReadable()) {
                    } else if (key.isWritable()) {

                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

}