package com.alibaba.manager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.Task;
import com.alibaba.config.QueueConfig;
import com.alibaba.queue.QueueChannel;
import com.alibaba.util.LogUtil;

/**
 * FileWriterChannel
 * include :
 * 1. write data and persistence
 * 2. force data to Disk
 * @author wxy.
 */
public class FileWriterChannel implements Task {

    private final ExecutorService writerTask;
    private boolean stopped = false;
    private final static long ASYNC_FLUSH_SECS = 2 * 1000;

    private MappedByteBuffer writeMappedByteBuffer;
    private FileChannel writeFileChannel;
    private final QueueChannel fileChannelQueue;

    public FileWriterChannel(QueueChannel fileChannelQueue) {
        this.fileChannelQueue = fileChannelQueue;

        //create a single thread
        this.writerTask = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<Runnable>(1), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "FileWriterChannel-WriterTask-");
            }
        });
    }

    /**
     * write data
     * @param message data
     */
    public synchronized void write(String message) {
        try {
            byte[] cb = message.getBytes(StandardCharsets.UTF_8);
            if (cb.length == 0) {
                return;
            }
            int messageSize = 4 + cb.length;
            //check data file has enough space
            checkWriteChannel(messageSize);

            writeMappedByteBuffer.putInt(cb.length);
            writeMappedByteBuffer.put(cb);
        } catch (Exception e) {
            throw new RuntimeException("Write message failed.", e);
        }

        fileChannelQueue.getLatch().countDown();
    }

    /**
     * force data to disk
     */
    private void asyncFlush() {
        while (!stopped) {
            try {
                if (writeMappedByteBuffer != null) {
                    writeMappedByteBuffer.force();
                }
            } catch (Exception e) {
                LogUtil.error("Writer flush exception. " + e);
            } finally {
                try {
                    Thread.sleep(ASYNC_FLUSH_SECS);
                } catch (InterruptedException e) {
                    // Nothing to do..
                }
            }
        }
    }

    /**
     * check data file has enough space
     *
     * @param messageSize
     */
    private void checkWriteChannel(int messageSize) {
        int rotationSize = QueueConfig.getInstance().getQueueSegmentSize();
        try {
            if (writeFileChannel == null) {
                writeFileChannel = new RandomAccessFile(fileChannelQueue.getDataFileManager()
                    .createRotationFile(), "rw").getChannel();
                writeMappedByteBuffer = writeFileChannel
                    .map(FileChannel.MapMode.READ_WRITE, 0, rotationSize);
            }
            if ((writeMappedByteBuffer.position() + messageSize) > rotationSize) {
                writeMappedByteBuffer.force();
                writeFileChannel.close();
                writeFileChannel = new RandomAccessFile(fileChannelQueue.getDataFileManager()
                    .createRotationFile(), "rw").getChannel();
                writeMappedByteBuffer = writeFileChannel
                    .map(FileChannel.MapMode.READ_WRITE, 0, rotationSize);
            }
        } catch (Exception e) {
            throw new RuntimeException("Create write channel failed", e);
        }
    }

    @Override
    public void start() {
        writerTask.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncFlush();
                } catch (Throwable t) {
                    LogUtil.error("asyncFlush() unhandle exception. " + t);
                }

                LogUtil.info("asyncFlush() stopped. ");
            }
        });
    }

    @Override
    public void stop() {
        stopped = true;
        writerTask.shutdownNow();
        if (null != writeMappedByteBuffer) {
            writeMappedByteBuffer.force();
        }
        try {
            if (null != writeFileChannel) {
                writeFileChannel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Close write channel failed.", e);
        }
    }
}
