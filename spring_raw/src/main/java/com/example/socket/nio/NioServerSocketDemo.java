package com.example.socket.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by dell
 * @Classname NioServerSocketDemo
 * @Description 客户端向服务端文件传输功能
 * @Date 2022/8/21 11:17
 */

class NioServerSocketDemo {

    private static String UPLOAD_SAVE_PATH = "D:\\works\\111";
    private static final Map<SelectableChannel, ReceiverFile> MAP = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        receiveFile();
    }

    private static void receiveFile() {
        getUploadSavePath();

        // 服务器端编写
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            // 绑定端口
            serverSocketChannel.bind(
                    new InetSocketAddress(
                            NioSocket.PORT
                    )
            );

            // 绑定选择器
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 轮训
            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    // 判断事件
                    if (key.isAcceptable()) {
                        accept(key, selector);
                    } else if (key.isReadable()) {
                        processData(key);
                    }
                }
            }

            selector.close();
            serverSocketChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void processData(SelectionKey key) throws IOException {
        ReceiverFile receiverFile = MAP.get(key.channel());

        SocketChannel socketChannel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(NioSocket.BUFFER_CAPACITY);

        int len = 0;

        while ((len = socketChannel.read(buffer)) > 0) {

            buffer.flip();

            if (receiverFile.fileName == null) {

                // 处理文件名称
                if (buffer.capacity() < 4) {
                    continue;
                }

                int fileNameLength = buffer.getInt();

                byte[] fileNameArr = new byte[fileNameLength];
                buffer.get(fileNameArr);

                String fileName = new String(fileNameArr, NioSocket.CHARSET);
                System.out.println("文件名称：" + fileName);
                receiverFile.fileName = fileName;

                // 处理存储文件
                File dir = new File(UPLOAD_SAVE_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }

                File file = new File((UPLOAD_SAVE_PATH + File.separator + fileName).trim());
                if (!file.exists()) {
                    file.createNewFile();
                }

                receiverFile.outChannel = new FileOutputStream(file).getChannel();

                // 长度
                if (buffer.capacity() < 8) {
                    continue;
                }

                long fileLength = buffer.getLong();
                System.out.println("文件大小：" + fileLength);
                receiverFile.length = fileLength;

                // 文件内容
                if (buffer.capacity() < 0) {
                    continue;
                }

                receiverFile.outChannel.write(buffer);
            } else {
                // 文件内容
                receiverFile.outChannel.write(buffer);
            }

            buffer.clear();
        }

        if (len == -1) {
            receiverFile.outChannel.close();
        }
    }

    private static void accept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();

        SocketChannel accept = channel.accept();
        accept.configureBlocking(false);

        accept.register(selector, SelectionKey.OP_READ);

        // 通道和File进行匹配
        ReceiverFile receiverFile = new ReceiverFile();
        MAP.put(accept, receiverFile);
    }

    private static void getUploadSavePath() {
        System.out.println("请输入想要保存文件的路劲：");
        Scanner scanner = new Scanner(System.in);
        UPLOAD_SAVE_PATH = scanner.next();
    }
}