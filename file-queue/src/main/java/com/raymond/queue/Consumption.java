package com.raymond.queue;


import com.raymond.queue.callback.ConsumeService;
import com.raymond.queue.utils.MappedByteBufferUtil;
import com.raymond.queue.utils.ProtostuffUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 消费者
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-14 10:41
 */
@SuppressWarnings("all")
public abstract class Consumption<E> {
    private final static Logger logger = LoggerFactory.getLogger(Consumption.class);

    private final static ThreadPoolExecutor CLEAN_FILE_THREAD_POOL = FileQueue.javaThreadExecutor("clean-file-thread");

    protected final BlockingQueue<CleanFile> cleanFileThreads = new LinkedBlockingQueue<>(10);
    /**
     * 类型
     */
    private final Class<E> eClass;
    /**
     * 路径
     */
    private final String path;
    /**
     * 主题
     */
    private final String topic;

    /**
     * 消费组
     */
    private final String groupName;

    /**
     * 文件队列信息
     */
    protected final FileQueue<E> fileQueue;

    private final long fileSize;

    private ReadFile readFile;

    protected FileMapped logFileMapped;

    protected FileMapped offsetFileMapped;

    protected long readOffset;

    protected long readIndex;

    /** 当前读文件的最大的offset **/
    protected long currLogMaxFileSize;
    /** 当前读文件的最大下标 **/
    protected long currOffsetMaxIndex;

    private long readLogName;

    private long readOffsetName;

    /** 读锁 **/
    protected final ReentrantLock readLock = new ReentrantLock();

    protected Consumption(Class<E> eClass, String path, String topic, String groupName, FileQueue<E> fileQueue, long fileSize) throws IOException {
        this.eClass = eClass;
        this.path = path + File.separator + topic + File.separator;
        this.topic = topic;
        this.groupName = groupName;
        this.fileQueue = fileQueue;
        this.fileSize = fileSize;
    }

    /**
     * 继续上一次消费
     * @param topic 主题
     * @param groupName 消费组名称
     * @throws IOException 异常
     */
    private void groupFromContinue(String groupName) throws IOException {
        String fileName = path + groupName + FileQueue.FileType.READ.name;
        this.readFile = new ReadFile(fileName);
        initFile();
    }

    /**
     * 从首条开始初始化
     * @param groupName 群组名称
     * @throws IOException io异常
     */
    private void groupFromFirst(String groupName) throws IOException {
        String fileName = path + groupName + FileQueue.FileType.READ.name;
        this.readFile = new ReadFile(fileName);
        setGroupFromFirst();
        initFile();
    }

    /**
     * 复制一份消费组文件
     * @param srcGroupName 来源消费组名称
     * @param groupName 新的消费组名称
     * @throws Exception 异常
     */
    private void groupFromSrc(String srcGroupName, String groupName) throws IOException {
        copyFile(srcGroupName, groupName);
        String fileName = path + groupName + FileQueue.FileType.READ.name;
        this.readFile = new ReadFile(fileName);
        initFile();
    }


    /**
     * 复制一份消费组文件
     * @param srcGroupName 来源消费组名称
     * @param groupName 新的消费组名称
     * @throws Exception 异常
     */
    private void copyFile(String srcGroupName, String groupName) throws IOException {
        ReentrantLock readLock = getSrcReentrantLock(srcGroupName);
        try {
            readLock.lock();
            Files.copy(Paths.get(path + srcGroupName + FileQueue.FileType.READ.name),
                    Paths.get(path + groupName + FileQueue.FileType.READ.name), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            readLock.unlock();
        }
    }


    private ReentrantLock getSrcReentrantLock(String srcGroupName) {
        Consumption<E> consumption = fileQueue.groupMap.get(srcGroupName);
        if (consumption == null) {
            return new ReentrantLock();
        } else {
            return consumption.readLock;
        }
    }


    /**
     * 设置首条信息
     * @throws IOException io
     */
    private void setGroupFromFirst() throws IOException {
        long readIndexName = MappedByteBufferUtil.getMinFileName(path, FileQueue.FileType.OFFSET_LIST.name);
        long readIndex = firstReadIndex(readIndexName);
        this.readFile.setReadIndex(readIndex == 0 ? 0 : readIndex);
    }

    /**
     * 寻找首条index
     * @param readIndex 当前offset的文件名(而最小的offset文件名代表者上个文件最后消费的index)
     * @return readIndex(已读的index)
     * @throws IOException
     */
    private long firstReadIndex(long readIndexName) throws IOException {
        String offsetFileName = path +
                String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", readIndexName) + FileQueue.FileType.OFFSET_LIST.name;
        try (FileMapped fileMapped = new FileMapped(offsetFileName, 0, 8)) {
            long readOffset = fileMapped.getMappedByteBuffer().getLong();
            long readLogName = MappedByteBufferUtil.getFileNameByOffset(path, FileQueue.FileType.LOG.name, readOffset, fileSize);
            if (readLogName != -1) {
                //readIndex是寻找最小的offset文件名,而最小的offset文件名代表者上个文件最后消费的index,存在上个文件被删除的情况
                //所以无法找到上个文件最后消费的已读的offset,所以readIndex需要加1,当前offset文件的第一条就代表上次已经消费过
                //等于0的情况下就已知已读的offset为0,所以不用加1
                return readIndexName == 0 ? 0 : readIndexName + 1;
            }
            long minFileName = MappedByteBufferUtil.getMinFileName(path, FileQueue.FileType.LOG.name);
            long maxOffset = MappedByteBufferUtil.getLong(fileMapped.getFileChannel(), fileSize - 8);
            if (minFileName < readOffset || (minFileName > maxOffset && maxOffset != 0)) {
                throw new RuntimeException("找不到首条log文件,最小的log文件名:" + minFileName +
                        ",当前的offset文件名:" + readIndexName + ",最小的offset:" + readOffset + ",最大的offset:" + maxOffset);
            }
            //因为MappedByteBufferUtil.getIndexByOffset(fileMapped.getFileChannel(), minFileName, fileSize)获取到的是当前文件中的offset
            //所以需要加上上次的index(index=readIndexName)
            return MappedByteBufferUtil.getIndexByOffset(fileMapped.getFileChannel(), minFileName, fileSize) + readIndexName;
        }
    }

    /**
     * 从最后一条开始
     * @param topic 主题
     * @param groupName 消费组
     * @throws IOException 异常
     */
    private void groupFromLast(String groupName) throws IOException {
        String fileName = path + groupName + FileQueue.FileType.READ.name;
        this.readFile = new ReadFile(fileName);
        setGroupFromLast();
        initFile();
    }

    /**
     * 从最后一条开始
     */
    private void setGroupFromLast() throws IOException {
        long readIndex = getWriteIndex();
        this.readFile.setReadIndex(readIndex);
    }



    /**
     * 对已有的消费组初始化
     * @param topic 主题
     * @param groupName 消费组名称
     * @throws IOException 异常
     */
    private void initFile() throws IOException {
        this.readIndex = this.readFile.getReadIndex();
        this.offsetFileMapped = getOffsetFileMapped();
        this.readOffset = initWriteOffset();
        this.logFileMapped = getLogFileMapped();
        this.currOffsetMaxIndex = (this.readIndex == 0 || this.readIndex % (fileSize / 8) != 0 ?
                (this.readIndex / (fileSize / 8) + 1) : this.readIndex / (fileSize / 8))
                * (fileSize / 8);

    }

    /**
     * 通过index找到offset
     * @return
     * @throws IOException
     */
    private long initWriteOffset() throws IOException {
        long readIndex = this.readIndex;
        if (readIndex == 0) {
            return 0;
        }
        long position = readIndex * 8 % fileSize - 8;
        if (readIndex * 8 % fileSize == 0) {
            position = fileSize - 8;
        }
        return MappedByteBufferUtil.getLong(this.offsetFileMapped.getFileChannel(), position);
    }

    /**
     * 获取log文件的文件映射
     * @param path 文件路径
     * @return log 文件的文件映射
     * @throws IOException
     */
    private FileMapped getLogFileMapped() throws IOException {
        long readLogName = MappedByteBufferUtil.getFileNameByOffset(path, FileQueue.FileType.LOG.name, readOffset, fileSize);
        if (readLogName < 0 || this.readOffset - readLogName > fileSize) {
            throw new RuntimeException("文件偏移量异常,获取的readLogName:" + readLogName);
        }
        String logFileName = path +
                String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", readLogName) + FileQueue.FileType.LOG.name;
        if (this.readIndex != 0 && !MappedByteBufferUtil.exists(logFileName)) {
            throw new RuntimeException("程序有误,需要读的文件找不到,文件名:" + logFileName);
        }
        logger.info("read:初始化log文件,文件名:{}", logFileName);
        this.currLogMaxFileSize = readLogName + fileSize;
        this.readLogName = readLogName;
        //最大offset减去上个文件最大的offset等于当前文件开始写的offset
        return new FileMapped(logFileName, this.readOffset - readLogName, readLogName + fileSize - this.readOffset);
    }


    /**
     * 获取offset文件的文件映射
     * @param path 文件路径
     * @return offset 文件的文件映射
     * @throws IOException
     */
    private FileMapped getOffsetFileMapped() throws IOException {
        long writeOffsetName = 0;
        if (this.readIndex != 0 && this.readIndex % (fileSize / 8) == 0) {
            writeOffsetName = this.readIndex - fileSize / 8;
        } else if (this.readIndex % (fileSize / 8) != 0){
            writeOffsetName = this.readIndex - this.readIndex % (fileSize / 8);
        }
        String offsetFileName = path +
                String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", writeOffsetName) + FileQueue.FileType.OFFSET_LIST.name;
        if (this.readIndex != 0 && !MappedByteBufferUtil.exists(offsetFileName)) {
            throw new RuntimeException("程序有误,需要读的文件找不到,文件名:" + offsetFileName);
        }
        logger.info("read:初始化offset文件,文件名:{}", offsetFileName);
        this.readOffsetName = writeOffsetName;
        //刚好写完上次文件没有扩容的情况
        if (this.readIndex != 0 && this.readIndex * 8 % fileSize == 0) {
            return new FileMapped(offsetFileName, fileSize, 0);
        }
        return new FileMapped(offsetFileName, readIndex * 8 % fileSize, fileSize - (readIndex * 8 % fileSize));
    }

    public E poll() {
        byte[] bytes = pollBytes();
        if (bytes == null) {
            return null;
        }
        return getData(bytes);
    }

    public byte[] pollBytes() {
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            byte[] bytes = pollBytes0();
            if (bytes != null) {
                writedisk();
            }
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
            return bytes;
        } finally {
            lock.unlock();
        }
    }

    protected <T> boolean consumeConfirm(ConsumeService<T> consumeService, T data,
                                         int offsetPosition, int logPosition, long readOffset) {
        try {
            boolean consumeStatus = consumeService.doService(data);
            if (consumeStatus) {
                writedisk();
            } else {
                rollback(offsetPosition, logPosition, readOffset);
            }
            return consumeStatus;
        } catch (Exception e) {
            rollback(offsetPosition, logPosition, readOffset);
            throw e;
        }
    }


    public boolean pollBytes(ConsumeService<List<byte[]>> consumeService, int count) {
        boolean consumeStatus = false;
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            List<byte[]> bytesList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                byte[] bytes = pollBytes0(i == 0);
                if (bytes != null) {
                    bytesList.add(bytes);
                } else {
                    break;
                }
            }
            if (bytesList != null && bytesList.size() > 0) {
                consumeStatus = consumeConfirm(consumeService, bytesList, offsetPosition, logPosition, readOffset);
            } else {
                consumeService.nullService();
            }
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }

    public boolean pollBytes(ConsumeService<byte[]> consumeService) {
        boolean consumeStatus = false;
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            byte[] bytes = pollBytes0(true);
            if (bytes != null) {
                consumeStatus = consumeConfirm(consumeService, bytes, offsetPosition, logPosition, readOffset);
            } else {
                consumeService.nullService();
            }
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean poll(ConsumeService<List<E>> consumeService, int count) {
        final ReentrantLock lock = this.readLock;
        lock.lock();
        boolean consumeStatus = false;
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            List<E> bytesList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                byte[] bytes = pollBytes0(i == 0);
                if (bytes != null) {
                    bytesList.add(getData(bytes));
                }
            }
            if (bytesList != null && bytesList.size() > 0) {
                consumeStatus = consumeConfirm(consumeService, bytesList, offsetPosition, logPosition, readOffset);
            } else {
                consumeService.nullService();
            }
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }

    public boolean poll(ConsumeService<E> consumeService) {
        final ReentrantLock lock = this.readLock;
        lock.lock();
        boolean consumeStatus = false;
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            byte[] bytes = pollBytes0(true);
            if (bytes != null) {
                consumeStatus = consumeConfirm(consumeService, getData(bytes), offsetPosition, logPosition, readOffset);
            } else {
                consumeService.nullService();
            }
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }

    protected byte[] pollBytes0(boolean isGrow) {
        if (!isRead()) {
            return null;
        }
        int len = getReadLength(isGrow);
        if (len == 0) {
            return null;
        }
        byte[] bytes = new byte[len];
        logFileMapped.getMappedByteBuffer().get(bytes, 0, len);
        readOffset += len;
        return bytes;
    }

    private int getReadLength(boolean isGrow) {
        if (isGrow && readIndex >= currOffsetMaxIndex) {
            offsetListReadGrow();
        } else if (readIndex >= currOffsetMaxIndex) {
            return 0;
        }
        int position = offsetFileMapped.getMappedByteBuffer().position();
        long readOffsetListLong = offsetFileMapped.getMappedByteBuffer().getLong();
        if (isGrow && readOffsetListLong > currLogMaxFileSize) {
            logReadGrow();
        } else if (readOffsetListLong > currLogMaxFileSize) {
            offsetFileMapped.getMappedByteBuffer().putLong(position);
            return 0;
        }
        int len = (int)(readOffsetListLong - readOffset);
        if (len > MappedByteBufferUtil.SINGLE_SIZE) {
            throw new RuntimeException("获取长度大于文件最大长度,队列异常,最大长度:" + MappedByteBufferUtil.SINGLE_SIZE + ",当前长度:" + len);
        }
        return len;
    }

    protected void rollback(int offsetPosition, int logPosition, long readOffset) {
        offsetFileMapped.getMappedByteBuffer().position(offsetPosition);
        logFileMapped.getMappedByteBuffer().position(logPosition);
        this.readOffset = readOffset;
    }

    protected void runCkeanFileThread() {
        for (;;) {
            CleanFile poll = cleanFileThreads.poll();
            if (poll == null) {
                return;
            }
            CLEAN_FILE_THREAD_POOL.execute(poll::delCompleteFile);
        }
    }

    protected E getData(byte[] bytes) {
        return ProtostuffUtils.deserializer(bytes, eClass);
    }

    /**
     * 写磁盘
     */
    protected void writedisk() {
        writedisk(1);
    }

    /**
     * 写磁盘
     */
    protected void writedisk(int size) {
        readIndex += size;
        readFile.setReadIndex(readIndex);
    }

    protected byte[] pollBytes0() {
        if (!isRead()) {
            return null;
        }
        int len = getReadLength();
        byte[] bytes = new byte[len];
        logFileMapped.getMappedByteBuffer().get(bytes, 0, len);
        readOffset += len;
        return bytes;
    }

    private int getReadLength() {
        if (readIndex >= currOffsetMaxIndex) {
            offsetListReadGrow();
        }
        long readOffsetListLong = offsetFileMapped.getMappedByteBuffer().getLong();
        if (readOffsetListLong > currLogMaxFileSize) {
            logReadGrow();
        }
        int len = (int)(readOffsetListLong - readOffset);
        if (len > MappedByteBufferUtil.SINGLE_SIZE) {
            throw new RuntimeException("获取长度大于文件最大长度,队列异常,最大长度:" + MappedByteBufferUtil.SINGLE_SIZE + ",当前长度:" + len);
        }
        return len;
    }


    protected void logReadGrow() {
        try {
            long readLogName = readOffset;
            String logFileName = path +
                    String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", readLogName) + FileQueue.FileType.LOG.name;
            if (!MappedByteBufferUtil.exists(logFileName)) {
                throw new RuntimeException("程序有误,需要读的文件找不到,文件名:" + logFileName);
            }
            logger.info("read:log文件扩容,文件名:{}", logFileName);
            logFileMapped.close();
            currLogMaxFileSize = readOffset + fileSize;
            logFileMapped = new FileMapped(logFileName, 0, fileSize);
            cleanFileThreads.put(new CleanFile(fileQueue, path, this.readLogName, FileQueue.FileType.LOG));
            this.readLogName = readLogName;
//            throw new RuntimeException("自定义异常");
        } catch (Exception e) {
            throw new RuntimeException("创建读取日志文件映射地址异常", e);
        }
    }

    protected void offsetListReadGrow() {
        try {
            long readOffsetName = readIndex;
            String offserFileName = path +
                    String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", readOffsetName) + FileQueue.FileType.OFFSET_LIST.name;
            if (!MappedByteBufferUtil.exists(offserFileName)) {
                throw new RuntimeException("程序有误,需要读的文件找不到,文件名:" + offserFileName);
            }
            logger.info("read:offset文件扩容,文件名:{}", offserFileName);
            //最大offset减去上个文件最大的offset等于当前文件开始写的offset
            offsetFileMapped.close();
            currOffsetMaxIndex += fileSize / 8;
            offsetFileMapped = new FileMapped(offserFileName, 0, fileSize);
            cleanFileThreads.put(new CleanFile(fileQueue, path, this.readOffsetName, FileQueue.FileType.OFFSET_LIST));
            this.readOffsetName = readOffsetName;
        } catch (Exception e) {
            throw new RuntimeException("创建读取日志文件映射地址异常", e);
        }
    }

    public long getReadIndex() {
        return this.readIndex;
    }

    public String getGroupName() {
        return groupName;
    }

    protected abstract boolean isRead();

    protected abstract long getWriteIndex();

    protected abstract long getWriteOffset();


    /**
     * 关闭资源
     * @throws IOException
     */
    public void close() throws IOException {
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            close0();
        } finally {
            lock.unlock();
        }
    }

    private void close0() throws IOException {
        ReadFile readFile = this.readFile;
        FileMapped logFileMapped = this.logFileMapped;
        FileMapped offsetFileMapped = this.offsetFileMapped;
        if (readFile != null) {
            readFile.close();
        }
        if (logFileMapped != null) {
            logFileMapped.close();
        }
        if (offsetFileMapped != null) {
            offsetFileMapped.close();
        }
        this.readFile = null;
        this.logFileMapped = null;
        this.offsetFileMapped = null;
    }

    protected void createConsumption(String groupName, FileQueue.GrowMode growMode, String srcGroupName, boolean isContinue) throws IOException {
        if (isContinue || growMode == FileQueue.GrowMode.CONTINUE_OFFSET) {
            if (fileQueue.existsGroup(groupName)) {
                groupFromContinue(groupName);
                growMode = FileQueue.GrowMode.CONTINUE_OFFSET;
            } else {
                logger.warn("当前消费组不存在,使用GrowMode.LAST_OFFSET模式创建消费组,groupName:{}", groupName);
                growMode = isContinue ?
                        (growMode != FileQueue.GrowMode.CONTINUE_OFFSET ? growMode : FileQueue.GrowMode.LAST_OFFSET) :
                        FileQueue.GrowMode.LAST_OFFSET;
            }
        }
        if (growMode == FileQueue.GrowMode.LAST_OFFSET) {
            groupFromLast(groupName);
        }
        if (growMode == FileQueue.GrowMode.COPY_GROUP) {
            if (!fileQueue.existsGroup(srcGroupName)) {
                throw new RuntimeException("来源的消费组不存在,不能够复制消费组,请选择已存在的消费组,srcGroupName:" + srcGroupName);
            }
            groupFromSrc(srcGroupName, groupName);
        }
        if (growMode == FileQueue.GrowMode.FIRST_OFFSET) {
            groupFromFirst(groupName);
        }
    }

    private class ReadFile {

        private final RandomAccessFile randomAccessFile;

        private final FileChannel fileChannel;

        private final List<MappedByteBuffer> mappedList = new ArrayList<>(4);
        /**
         * 最大的offset
         */
        private MappedByteBuffer readIndex;

        private ReadFile(String fileName) throws IOException {
            randomAccessFile = new RandomAccessFile(fileName, "rw");
            fileChannel = randomAccessFile.getChannel();
            getReadMap();
        }

        /**
         * 获取读文件的MappedByteBuffer
         * 0-7代表已读的index条数(每读一条更新:readIndex)
         * 8-9代表文件是否启动,0:未启动,1:启动 (待定)
         * 10-18代表文件的心跳时间,最后一次更新心跳时间 (待定)
         */
        private void getReadMap() throws IOException {
            List<MappedByteBuffer> mappedList = new ArrayList<>(6);
            this.readIndex = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
            mappedList.add(readIndex);
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

        public long getReadIndex() {
            long value = this.readIndex.getLong();
            this.readIndex.flip();
            return value;
        }

        private void setReadIndex(long index) {
            readIndex.putLong(index);
            readIndex.flip();
        }

        private void force() throws IOException {
            fileChannel.force(true);
        }
    }
}
