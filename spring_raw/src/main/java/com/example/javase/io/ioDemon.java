package com.example.javase.io;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @author: WuQi
 * @time: 2020/2/6 10:57
 */

public class ioDemon {
    public static void listAllFiles(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        if (dir.isFile()) {
            System.out.println(dir.getName());
            return;
        }
        for (File file : dir.listFiles()) {
            listAllFiles(file);
        }
    }

    public static void copyFile(String src, String dist) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dist);

        byte[] buffer = new byte[20 * 1024];
        int cnt;

        while ((cnt = in.read(buffer, 0, buffer.length)) != -1){
            out.write(buffer, 0, cnt);
        }
        in.close();
        out.close();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String string1 = "中文";
        byte[] bytes = string1.getBytes(StandardCharsets.UTF_8);
//        System.out.println(bytes);
        String string2 = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(string2);
    }
}
