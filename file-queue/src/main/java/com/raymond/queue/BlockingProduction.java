package com.raymond.queue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阻塞生产者
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-22 14:58
 */
public class BlockingProduction<E> extends Production<E> {
    private Condition condition;
    protected BlockingProduction(String path, String topic, long fileSize) throws IOException {
        super(path, topic, fileSize);
        condition = super.writeLock.newCondition();
    }


    @Override
    public void put(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new NullPointerException("bytes is null");
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            log(bytes);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(E e) {
        if (e == null) {
            throw new NullPointerException("e is null");
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            log(getBytes(e));
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

   void wait0() throws InterruptedException {
        ReentrantLock writeLock = super.writeLock;
        writeLock.lock();
        try {
            condition.await();
        } finally {
            writeLock.unlock();
        }
    }

    void wait0(long time, TimeUnit timeUnit) throws InterruptedException {
        ReentrantLock writeLock = super.writeLock;
        writeLock.lock();
        try {
            long nanosTimeout = timeUnit.toNanos(time);
            condition.awaitNanos(nanosTimeout);
        } finally {
            writeLock.unlock();
        }
    }

    private void signalAll() {
        ReentrantLock writeLock = super.writeLock;
        writeLock.lock();
        try {
            condition.signalAll();
        } finally {
            writeLock.unlock();
        }
    }

}
