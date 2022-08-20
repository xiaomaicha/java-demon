package com.raymond.queue;

import com.raymond.queue.callback.ConsumeService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阻塞的文件队列
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-22 14:45
 */
public class BlockingConsumption<E> extends Consumption<E> {

    private BlockingProduction<E> production;


    protected BlockingConsumption(Class<E> eClass, String path, String topic, String groupName, FileQueue<E> fileQueue,
                                  FileQueue.GrowMode growMode, String srcGroupName, long fileSize, boolean isContinue) throws Exception {
        super(eClass, path, topic, groupName, fileQueue, fileSize);
        if (fileQueue.getProduction() == null) {
            throw new RuntimeException("没有创建生产者,不予许使用阻塞消费者");
        }
        if (!BlockingProduction.class.isAssignableFrom(fileQueue.getProduction().getClass())) {
            throw new RuntimeException("必须是阻塞生产者,不予许使用阻塞消费者");
        }
        this.production = (BlockingProduction<E>) fileQueue.getProduction();
        super.createConsumption(groupName, growMode, srcGroupName, isContinue);
    }

    @Override
    protected boolean isRead() {
        return readOffset < production.getWriteOffset();
    }


    @Override
    protected long getWriteIndex() {
        return production.getWriteIndex();
    }

    @Override
    protected long getWriteOffset() {
        return production.getWriteOffset();
    }


    public E take() throws InterruptedException {
        E poll;
        while ((poll = super.poll()) == null) {
            wait0();
        }
        return poll;
    }

    public byte[] takeBytes() throws InterruptedException {
        byte[] poll;
        while ((poll = super.pollBytes()) == null) {
            wait0();
        }
        return poll;
    }
    private void wait0() throws InterruptedException {
        while (readOffset >= production.getWriteOffset()) {
            production.wait0();
        }
    }

    private void wait0(long time, TimeUnit timeUnit) throws InterruptedException {
        production.wait0(time, timeUnit);
    }

    public boolean takeBytes(ConsumeService<List<byte[]>> consumeService, int count, long timeout, TimeUnit timeUnit) throws InterruptedException {
        boolean consumeStatus;
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            List<byte[]> bytesList = new ArrayList<>();
            byte[] bytes;
            while ((bytes = super.pollBytes0(true)) == null) {
                wait0();
            }
            bytesList.add(bytes);
            long deadline = System.nanoTime() + timeUnit.toNanos(timeout);
            int added = 1;
            while (added < count) {
                bytes = pollBytes0(false);
                if (bytes != null) {
                    bytesList.add(bytes);
                    added++;
                    continue;
                }
                long time = deadline - System.nanoTime();
                if (time <= 0) {
                    break;
                }
                wait0(time, timeUnit);
            }
            consumeStatus = consumeConfirm(consumeService, bytesList, offsetPosition, logPosition, readOffset);
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }

    public boolean take(ConsumeService<List<E>> consumeService, int count, long timeout, TimeUnit timeUnit) throws InterruptedException {
        boolean consumeStatus;
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            List<E> bytesList = new ArrayList<>();
            byte[] bytes;
            while ((bytes = super.pollBytes0(true)) == null) {
                wait0();
            }
            bytesList.add(getData(bytes));
            long deadline = System.nanoTime() + timeUnit.toNanos(timeout);
            int added = 1;
            while (added < count) {
                bytes = pollBytes0(false);
                if (bytes != null) {
                    bytesList.add(getData(bytes));
                    added++;
                    continue;
                }
                long time = deadline - System.nanoTime();
                if (time <= 0) {
                    break;
                }
                wait0(time, timeUnit);
            }
            consumeStatus = consumeConfirm(consumeService, bytesList, offsetPosition, logPosition, readOffset);
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }


    public boolean takeBytes(ConsumeService<byte[]> consumeService) throws InterruptedException {
        boolean consumeStatus;
        final ReentrantLock lock = this.readLock;
        lock.lock();
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            byte[] bytes;
            while ((bytes = super.pollBytes0(true)) == null) {
                wait0();
            }
            consumeStatus = consumeConfirm(consumeService, bytes, offsetPosition, logPosition, readOffset);
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }

    public boolean take(ConsumeService<E> consumeService) throws InterruptedException {
        final ReentrantLock lock = this.readLock;
        lock.lock();
        boolean consumeStatus = false;
        try {
            int offsetPosition = offsetFileMapped.getMappedByteBuffer().position();
            int logPosition = logFileMapped.getMappedByteBuffer().position();
            long readOffset = this.readOffset;
            byte[] bytes;
            while ((bytes = super.pollBytes0(true)) == null) {
                wait0();
            }
            consumeStatus = consumeConfirm(consumeService, getData(bytes), offsetPosition, logPosition, readOffset);
            if (!cleanFileThreads.isEmpty()) {
                runCkeanFileThread();
            }
        } finally {
            lock.unlock();
        }
        return consumeStatus;
    }



}
