package com.example.javase.io;

/**
 * @author by dell
 * @Classname FileTest
 * @Description
 * @Date 2022/8/21 18:33
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileTest {
    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("out.txt", "rw");
        raf.write("hello haha\n".getBytes());
        raf.write("hello lingqingxia\n".getBytes());
        System.out.println("write------------");
        System.in.read();
        raf.seek(4);
        raf.write("ooxx".getBytes());
        System.out.println("seek---------");
        System.in.read();
        FileChannel rafchannel = raf.getChannel();
        MappedByteBuffer map = rafchannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
        map.put("@@@".getBytes());
        // 不是系统调用  但是数据会到达 内核的pagecache
        System.out.println("map--put--------");
        System.in.read();
        raf.seek(0);
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int read = rafchannel.read(buffer);
        System.out.println(buffer);
        buffer.flip();
        System.out.println(buffer);
        for (int i = 0; i < buffer.limit(); i++) {
            Thread.sleep(200);
            System.out.print(((char) buffer.get(i)));
        }
    }
}
