package com.raymond.queue;


import com.dyuproject.protostuff.LinkedBuffer;
import com.raymond.queue.utils.MappedByteBufferUtil;
import com.raymond.queue.utils.ProtostuffUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-11 10:03
 */
@SuppressWarnings("all")
public class Production<E> {

    private final static Logger logger = LoggerFactory.getLogger(Production.class);

    private final String path;

    protected final String topic;

    private WriteFile writeFile;

    private final long fileSize;
    /**
     * 当前写的最大的offset
     */
    private long currLogMaxFileSize;
    /**
     * 写的最大下标
     */
    private long currOffsetMaxIndex;

    private FileMapped logFileMapped;

    private FileMapped offsetFileMapped;
    
    private long writeOffset;
   
    private long writeIndex;

    protected final ReentrantLock writeLock = new ReentrantLock();

    /**
     * 避免每次序列化都重新申请Buffer空间
     */
    protected final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    protected Production(String path, String topic, long fileSize) throws IOException {
        this.path = path + File.separator + topic + File.separator;
        this.topic = topic;
        this.fileSize = fileSize;
        initFile();
    }

    /**
     * 初始化文件
     * @throws IOException io异常
     */
    private void initFile() throws IOException {
        String fileName = path + "queue" + FileQueue.FileType.WRITE.name;
        this.writeFile = new WriteFile(fileName);
        this.writeIndex = writeFile.getWriteIndex();
        this.offsetFileMapped = getOffsetFileMapped();
        this.writeOffset = initWriteOffset();
        this.logFileMapped = getLogFileMapped();
        this.currOffsetMaxIndex = (this.writeIndex == 0 || this.writeIndex % (fileSize / 8) != 0 ?
                (this.writeIndex / (fileSize / 8) + 1) : this.writeIndex / (fileSize / 8))
                * (fileSize / 8);
    }

    /**
     * 通过index找到offset
     * @return
     * @throws IOException
     */
    private long initWriteOffset() throws IOException {
        long writeIndex = this.writeIndex;
        if (writeIndex == 0) {
            return 0;
        }
        long position = writeIndex * 8 % fileSize - 8;
        if (writeIndex * 8 % fileSize == 0) {
            position = fileSize - 8;
        }
        FileChannel fileChannel = this.offsetFileMapped.getFileChannel();
        MappedByteBuffer map = null;
        try {
            map = fileChannel.map(FileChannel.MapMode.READ_WRITE,
                    position, 8);
            return map.getLong();
        } finally {
            MappedByteBufferUtil.clean(map, fileChannel);
        }
    }

    public void put(E e) {
        if (e == null) {
            throw new NullPointerException("e is null");
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            log(getBytes(e));
        } finally {
            lock.unlock();
        }
    }

    public void put(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new NullPointerException("e is null");
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            log(bytes);
        } finally {
            lock.unlock();
        }
    }

    protected byte[] getBytes(E e) {
        return ProtostuffUtils.serializer(e, buffer);
    }
    /**
     * 将队列数据写文件
     * @param log 需要写的数据
     */
    protected void log(byte[] bytes) {
        if (bytes.length > MappedByteBufferUtil.SINGLE_SIZE) {
            throw new RuntimeException("数据超长,最大长度:" + MappedByteBufferUtil.SINGLE_SIZE + ",当前长度:" + bytes.length);
        }
        long offset = writeOffset + bytes.length;
        if (offset > currLogMaxFileSize) {
            logWriteGrow();
        }
        logFileMapped.getMappedByteBuffer().put(bytes);
        //每条追加一条offset
        if(++writeIndex > currOffsetMaxIndex) {
            offsetListWriteGrow();
        }
        offsetFileMapped.getMappedByteBuffer().putLong(offset);
        writedisk();
        writeOffset = offset;
    }

    /**
     * 写磁盘
     */
    private void writedisk() {
        writeFile.setWriteIndex(writeIndex);
    }

    /**
     * 获取当前已写的offset
     * @return
     */
    public long getWriteOffset() {
        return writeOffset;
    }
    /**
     * 获取当前已写的index
     * @return
     */
    public long getWriteIndex() {
        return writeIndex;
    }

    private void offsetListWriteGrow() {
        try {
            long writeOffsetName = this.writeIndex - 1;
            String offsetFileName = path +
                    String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", writeOffsetName) + FileQueue.FileType.OFFSET_LIST.name;
            logger.info("write:offset文件扩容,文件名:{}", offsetFileName);
            //最大offset减去上个文件最大的offset等于当前文件开始写的offset
            offsetFileMapped.close();
            logFileMapped.force();
            writeFile.force();
//            writeFile.putValue(2, writeOffset);
//            writeFile.putOffsetIndexMapped(writeOffset, this.writeIndex - 1);
            currOffsetMaxIndex += fileSize / 8;
            offsetFileMapped = new FileMapped(offsetFileName, 0, fileSize);
        } catch (Exception e) {
            throw new RuntimeException("创建读取日志文件映射地址异常", e);
        }
    }

    public void close() throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            close0();
        } finally {
            lock.unlock();
        }
    }

    private void close0() throws IOException {
        WriteFile writeFile = this.writeFile;
        FileMapped logFileMapped = this.logFileMapped;
        FileMapped offsetFileMapped = this.offsetFileMapped;
        if (writeFile != null) {
            writeFile.close();
        }
        if (logFileMapped != null) {
            logFileMapped.close();
        }
        if (offsetFileMapped != null) {
            offsetFileMapped.close();
        }
        this.writeFile = null;
        this.logFileMapped = null;
        this.offsetFileMapped = null;
    }

    private void logWriteGrow() {
        try {
            long writeLogName = writeOffset;
            String logFileName = path +
                    String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", writeLogName) + FileQueue.FileType.LOG.name;
            logger.info("write:log文件扩容,文件明:{}", logFileName);
            //最大offset减去上个文件最大的offset等于当前文件开始写的offset
            logFileMapped.close();
            offsetFileMapped.force();
            writeFile.force();
//            writeFile.putValue(1, writeOffset);
            currLogMaxFileSize = writeOffset + fileSize;
            logFileMapped = new FileMapped(logFileName, 0, fileSize);
        } catch (Exception e) {
            throw new RuntimeException("创建读取日志文件映射地址异常", e);
        }
    }

    /**
     * 获取log文件的文件映射
     * @param path 文件路径
     * @return log 文件的文件映射
     * @throws IOException
     */
    private FileMapped getLogFileMapped() throws IOException {
        long writeLogName = MappedByteBufferUtil.getFileNameByOffset(path, FileQueue.FileType.LOG.name, writeOffset, fileSize);
        if (writeLogName < 0 || this.writeOffset - writeLogName > fileSize || this.writeOffset < writeLogName) {
            throw new RuntimeException("文件偏移量异常,获取的writeLogName:" + writeLogName + ",当前需要写入的offset:" + writeOffset);
        }
        String logFileName = path +
                String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", writeLogName) + FileQueue.FileType.LOG.name;
        if (this.writeIndex != 0 && !MappedByteBufferUtil.exists(logFileName)) {
            throw new RuntimeException("程序有误,需要读的文件找不到,文件名:" + logFileName);
        }
        logger.info("write:初始化log文件,文件名:{}", logFileName);
        this.currLogMaxFileSize = writeLogName + fileSize;
        //最大offset减去上个文件最大的offset等于当前文件开始写的offset
        return new FileMapped(logFileName, this.writeOffset - writeLogName, writeLogName + fileSize - this.writeOffset);
    }


    /**
     * 获取offset文件的文件映射
     * @param path 文件路径
     * @return offset 文件的文件映射
     * @throws IOException
     */
    private FileMapped getOffsetFileMapped() throws IOException {
        long writeOffsetName = 0;
        if (this.writeIndex != 0 && this.writeIndex % (fileSize / 8) == 0) {
            writeOffsetName = this.writeIndex - fileSize / 8;
        } else if (this.writeIndex % (fileSize / 8) != 0){
            writeOffsetName = this.writeIndex - this.writeIndex % (fileSize / 8);
        }
        String logFileName = path +
                String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", writeOffsetName) + FileQueue.FileType.OFFSET_LIST.name;
        if (this.writeIndex != 0 && !MappedByteBufferUtil.exists(logFileName)) {
            throw new RuntimeException("程序有误,需要读的文件找不到,文件名:" + logFileName);
        }
        logger.info("write:初始化offset文件,文件名:{}", logFileName);
        //刚好写完上次文件没有扩容的情况
        if (this.writeIndex != 0 && this.writeIndex * 8 % fileSize == 0) {
            return new FileMapped(logFileName, fileSize, 0);
        }
        return new FileMapped(logFileName, writeIndex * 8 % fileSize, fileSize - (writeIndex * 8 % fileSize));
    }



    private class WriteFile {

        private final RandomAccessFile randomAccessFile;

        private final FileChannel fileChannel;

        private final List<MappedByteBuffer> mappedList = new ArrayList<>(3);

        /**
         * 最大的offset
         */
        private MappedByteBuffer writeIndex;

        private WriteFile(String fileName) throws IOException {
            randomAccessFile = new RandomAccessFile(fileName, "rw");
            fileChannel = randomAccessFile.getChannel();
            getWriteMap();
        }

        /**
         * 获取读文件的MappedByteBuffer
         * 0-7代表已写的index条数(每写一条更新:writeIndex)
         * 8-9代表文件是否启动,0:未启动,1:启动 (待定)
         * 10-18代表文件的心跳时间,最后一次更新心跳时间 (待定)
         */
        private void getWriteMap() throws IOException {
            List<MappedByteBuffer> mappedList = new ArrayList<>(6);
            this.writeIndex = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
            mappedList.add(writeIndex);
            this.mappedList.addAll(mappedList);
        }

        public void close() throws IOException {
            mappedList.forEach((mappedByteBuffer) -> {
                    MappedByteBufferUtil.clean(mappedByteBuffer, fileChannel);
            });
            force();
            fileChannel.close();
            randomAccessFile.close();
        }

        public long getWriteIndex() {
            long value = this.writeIndex.getLong();
            this.writeIndex.flip();
            return value;
        }

        private void setWriteIndex(long index) {
            writeIndex.putLong(index);
            writeIndex.flip();
        }

        private void force() throws IOException {
            fileChannel.force(true);
        }
    }
}
