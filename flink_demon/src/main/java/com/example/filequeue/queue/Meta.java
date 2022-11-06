package com.example.filequeue.queue;

import com.example.filequeue.util.CheckSum;

/**
 * @author wxy.
 */
public class Meta {
    private volatile int readPos;
    private volatile long checkSum;
    private volatile String fileName;

    public int getReadPos() {
        return readPos;
    }

    public long getCheckSum() {
        return checkSum;
    }

    public String getFileName() {
        return fileName;
    }

    public static String concat(int readPos, String fileName) {
        if (fileName == null) {
            return String.valueOf(readPos);
        }
        return readPos + fileName;
    }

    public void set(int readPos, String fileName) {
        this.readPos = readPos;
        this.fileName = fileName;
        this.checkSum = CheckSum.adler32CheckSum(concat(readPos, fileName));
    }
}
