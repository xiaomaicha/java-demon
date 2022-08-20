package com.raymond.queue.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Mapp工具类
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2021-01-14 10:21
 */
public class MappedByteBufferUtil {

    public static final long FILE_SIZE = 100 * 1024 * 1024;

    public static final long SINGLE_SIZE = 1024 * 1024;
    /**
     * 文件名长度
     */
    public static final int NAME_LEN = 19;

    /**
     * 通过offset获取文件名
     * @param path 文件路径
     * @param fileType 文件尾缀
     * @return 文件名
     */
    public static long getMinFileName(String path, String fileType) {
        File[] files = getFiles(path, fileType);
        if (files == null || files.length == 0) {
            return 0;
        }
        return Long.parseLong(files[0].getName().substring(0, NAME_LEN));
    }

    /**
     * 通过offset寻找文件名
     * offset等于文件名,直接取文件名
     * 文件名必须在offset - (offset+fileSize)之间才是这个文件
     * -1代表找不到文件
     * @param path 文件路径
     * @param fileType 文件尾缀
     * @param offset offset
     * @param fileSize 文件大小
     * @return 文件名
     */
    public static long getFileNameByOffset(String path, String fileType, long offset, long fileSize) {
        File[] files = getFiles(path, fileType);
        if (files == null || files.length == 0) {
            return 0;
        }
        for (File file : files) {
            String name = file.getName();
            long offsetName = Long.parseLong(name.substring(0, NAME_LEN));
            if (offsetName == offset) {
                return offsetName;
            }
            if (offsetName < offset && offsetName >= offset - fileSize) {
                return offsetName;
            }
        }
        return -1;
    }

    public static File[] getFiles(String path, String fileType) {
        File file = new File(path);
        File[] files = file.listFiles((dir, name) -> name.endsWith(fileType));
        if (files == null || files.length == 0) {
            return null;
        }
        Arrays.sort(files, Comparator.comparingLong(f -> Long.parseLong(f.getName().substring(0, NAME_LEN))));
        return files;
    }

    public static long getIndexByOffset(FileChannel fileChannel, long currOffset, long fileSize) throws IOException {
        int min = 0;
        int max = (int) fileSize / 8;
        int count = 64;
        for (int i = 0; i < count; i++) {
            int index = (min + max) / 2;
            long offset = MappedByteBufferUtil.getLong(fileChannel, index * 8);
            if (offset == currOffset) {
                //index是从0开始,实际却是从1开始,所以需要加1
                return index + 1;
            }
            if (offset == 0 || offset > currOffset) {
                max = index;
                continue;
            }
            min = index;
        }
        throw new RuntimeException("找不到对应的offset");
    }

    /**
     * 清空MappedByteBuffer的数据
     *
     * @param buffer 需要清除的buffer
     * @param fileChannel 需要清除的fileChannel
     */
    public static void clean(final MappedByteBuffer buffer, FileChannel fileChannel)  {
        if (null == buffer) {
            return;
        }
        try {
            Method m = fileChannel.getClass().getDeclaredMethod("unmap", MappedByteBuffer.class);
            m.setAccessible(true);
            m.invoke(fileChannel.getClass(), buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long getLong(FileChannel fileChannel, long position) throws IOException {
        MappedByteBuffer map = null;
        try {
            map = fileChannel.map(FileChannel.MapMode.READ_WRITE, position, 8);
            return map.getLong();
        } finally {
            clean(map, fileChannel);
        }
    }

    /**
     * 获取当前buffer的值(long类型)
     * @param buffer buffer
     * @return 读取的值
     */
    public static long getLongFromBuffer(MappedByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            buffer.flip();
        }
        return buffer.getLong();

    }
    /**
     * 获取当前buffer的值(int类型)
     * @param buffer buffer
     * @return 读取的值
     */
    public static long getIntFromBuffer(MappedByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            buffer.flip();
        }
        return buffer.getInt();
    }

    /**
     * 存值到MappedByteBufferUtil
     * @param buffer 需要存入的MappedByteBufferUtil
     * @param value 需要存入的值
     */
    public static void putIntToBuffer(MappedByteBuffer buffer, int value) {
        if (!buffer.hasRemaining()) {
            buffer.flip();
        }
        buffer.putInt(value);
    }

    /**
     * 是否运行
     * @param isStart 运行判断MappedByteBuffer
     * @param lastRunTime 运行时间MappedByteBuffer
     * @return true:运行,false:停止
     */
    public static boolean isRun(MappedByteBuffer isStart, MappedByteBuffer lastRunTime) {
        long intIsStart = getIntFromBuffer(isStart);
        long longLastRunTime = getLongFromBuffer(lastRunTime);
        long timeMillis = System.currentTimeMillis();
        return intIsStart != 0 && timeMillis - longLastRunTime < 30 * 1000;
    }

    public static boolean isStrEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean exists(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.isFile();
    }

    /**
     * 是否是集合类型
     * @param eClass 类型
     * @return true 是集合
     */
    public static boolean isCollection(Class<?> eClass) {
        if (List.class.isAssignableFrom(eClass)) {
            return true;
        }
        if (Set.class.isAssignableFrom(eClass)) {
            return true;
        }
        return Map.class.isAssignableFrom(eClass);
    }

}
