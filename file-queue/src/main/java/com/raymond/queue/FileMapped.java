package com.raymond.queue;

import com.raymond.queue.utils.MappedByteBufferUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件映射mmap
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-04 10:15
 */
public class FileMapped implements Closeable {

    private final RandomAccessFile randomAccessFile;

    private final FileChannel fileChannel;

    private final MappedByteBuffer mappedByteBuffer;


    FileMapped(String fileName, long position, long size) throws IOException {
        randomAccessFile = new RandomAccessFile(fileName, "rw");
        fileChannel = randomAccessFile.getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, position, size);
    }


    @Override
    public void close() throws IOException {
        MappedByteBufferUtil.clean(mappedByteBuffer, fileChannel);
        force();
        fileChannel.close();
        randomAccessFile.close();
    }

    public void force() throws IOException {
        fileChannel.force(true);
    }


    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    FileChannel getFileChannel() {
        return fileChannel;
    }

    MappedByteBuffer getMappedByteBuffer() {
        return mappedByteBuffer;
    }
}
