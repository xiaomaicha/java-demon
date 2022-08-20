package com.raymond.queue.utils;

import com.raymond.queue.FileQueue;

/**
 * 构建文件队列
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2021-01-18 15:18
 */
public class FileQueueBuilder<E> {
    /**
     * 泛型类型
     */
    private final Class<E> eClass;
    /**
     * 文件地址
     */
    private String path;
    /**
     * 主题名称
     */
    private final String topic;
    /**
     * 队列模型
     */
    private FileQueue.QueueModel queueModel;
    /**
     * 消费组创建的类型
     */
    private FileQueue.GrowMode growMode;
    /**
     * 消费组名称
     */
    private String groupName;
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * 消费组是否继续上次消费
     */
    private boolean isContinue = true;

    public static <E> FileQueueBuilder<E> create(Class<E> eClass, String topic) {
        return new FileQueueBuilder<>(eClass, topic);
    }

    private FileQueueBuilder(Class<E> eClass, String topic) {
        this.eClass = eClass;
        this.topic = topic;
        this.path = FileQueue.DEFAULT_PATH;
        this.queueModel = FileQueue.QueueModel.ORDINARY;
        this.growMode = FileQueue.GrowMode.CONTINUE_OFFSET;
        this.groupName = FileQueue.DEFAULT_GROUP;
        this.fileSize = MappedByteBufferUtil.FILE_SIZE;
    }

    public FileQueueBuilder<E> setPath(String path) {
        this.path = path;
        return this;
    }

    public FileQueueBuilder<E> setQueueModel(FileQueue.QueueModel queueModel) {
        this.queueModel = queueModel;
        return this;
    }

    public FileQueueBuilder<E> setGrowMode(FileQueue.GrowMode growMode) {
        this.growMode = growMode;
        return this;
    }

    public FileQueueBuilder<E> setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public FileQueueBuilder<E> setFileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public FileQueueBuilder<E> setContinue(boolean aContinue) {
        isContinue = aContinue;
        return this;
    }

    public FileQueue<E> build() throws Exception {
        return FileQueue.instantiation(eClass, path, topic, groupName, queueModel, growMode, fileSize, isContinue);
    }


}
