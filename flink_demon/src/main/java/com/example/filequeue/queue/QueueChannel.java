package com.example.filequeue.queue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.filequeue.manager.*;
import com.example.filequeue.queue.AbstractQueue;
import com.example.filequeue.queue.MessageWrapper;
import com.example.filequeue.util.LogUtil;

/**
 * queue definition
 *
 * @author wxy.
 */
public class QueueChannel extends AbstractQueue {

    private final MetaFileManager metaManager;
    private final DataFileManager dataFileManager;
    private final FileWriterChannel writer;
    private final FileReaderChannel reader;
    private MessageWrapper lastMessage;
    private final LinkedBlockingQueue<MessageWrapper> queue;
    private final String queueName;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final FileMonitor fileMonitor;

    public QueueChannel(String queueName) {
        super(queueName);
        this.queueName = queueName;
        this.metaManager = new MetaFileManager(queueName);
        this.dataFileManager = new DataFileManager(queueName);
        this.writer = new FileWriterChannel(this);
        this.reader = new FileReaderChannel(this);
        this.queue = new LinkedBlockingQueue<>(100000);
        this.fileMonitor = new FileMonitor();

    }


    public String getQueueName() {
        return queueName;
    }

    public MetaFileManager getMetaManager() {
        return metaManager;
    }

    public DataFileManager getDataFileManager() {
        return dataFileManager;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public LinkedBlockingQueue<MessageWrapper> getQueue() {
        return queue;
    }

    @Override
    public void offer(String data) {
        writer.write(data);
    }

    @Override
    public MessageWrapper getByPop() {
        MessageWrapper messageWrapper = null;
        try {
            messageWrapper = queue.poll();
            lastMessage = messageWrapper;
        } catch (Exception e) {
            LogUtil.warn("Exception when pop message from QueueChannel. " + e);
        }
        return messageWrapper;
    }

    @Override
    public MessageWrapper getByTake() {
        MessageWrapper messageWrapper = null;
        try {
            messageWrapper = queue.take();
            lastMessage = messageWrapper;
        } catch (Exception e) {
            LogUtil.warn("Exception when take message from QueueChannel. " + e);
        }
        return messageWrapper;
    }

    @Override
    public void start() {
        reader.start();
        writer.start();
        fileMonitor.start();
    }

    @Override
    public void stop() {
        reader.stop();
        writer.stop();
        fileMonitor.stop();
        metaManager.close();
    }

    @Override
    public void confirmLastMessage() {
        if (lastMessage != null) {
            metaManager.update(lastMessage.getEndPos(), lastMessage.getCurrentFile());
            if (lastMessage.isFirstMessage()) {
                dataFileManager.deleteOlderFiles(lastMessage.getCurrentFile());
            }
        }
    }
}
