package com.example.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

/**
 * @author by dell
 * @Classname DatagramChannel
 * @Description
 * @Date 2022/8/21 16:21
 */
public class DatagramOpenChannel {
    public static void main(String[] args) {
        int port = getPort();
        datagramOpenChannel(port);
    }

    private static int getPort() {
        System.out.println("请输入你要绑定的端口号：");
        Scanner scanner = new Scanner(System.in);

        return scanner.nextInt();

    }

    private static void datagramOpenChannel(int port) {

        DatagramChannel open = null;
        try {
            open = DatagramChannel.open();
            open.configureBlocking(false);

            open.bind(new InetSocketAddress(port));

            read(open);

            send(open);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != open) {
                try {
                    open.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void send(DatagramChannel open) throws IOException {
        System.out.println("输入的内容格式：port@msg");

        Scanner scanner = new Scanner(System.in);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (scanner.hasNext()) {
            String next = scanner.next();
            if (next.contains("@")) {
                String[] split = next.split("@");
                int port = Integer.parseInt(split[0]);
                String msg = split[1];

                buffer.put(msg.getBytes());

                buffer.flip();
                open.send(buffer, new InetSocketAddress("127.0.0.1", port));
                buffer.clear();
            }
        }
    }

    private static void read(DatagramChannel open) throws IOException {
        new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                try {
                    SocketAddress receive = open.receive(buffer);
                    if (null != receive) {
                        buffer.flip();
                        System.out.println(new String(buffer.array(), 0, buffer.limit()));
                        buffer.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}