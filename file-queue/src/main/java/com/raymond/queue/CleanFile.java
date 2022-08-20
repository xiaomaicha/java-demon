package com.raymond.queue;

import com.raymond.queue.utils.MappedByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 清理文件线程
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-15 11:40
 */
public class CleanFile  {
    private final static Logger logger = LoggerFactory.getLogger(CleanFile.class);

    private FileQueue fileQueue;

    private String path;

    private long fileName;

    private FileQueue.FileType fileType;

    CleanFile(FileQueue fileQueue, String path, long fileName, FileQueue.FileType fileType) {
        this.fileQueue = fileQueue;
        this.path = path;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public void delCompleteFile() {
        try {
            if (fileType == FileQueue.FileType.LOG) {
                delLogFile();
            }
            if (fileType == FileQueue.FileType.OFFSET_LIST) {
                delOffsetFile();
            }
        } catch (Exception e) {
            logger.error("清除文件异常,文件路径:{},文件名:{},文件类型:{}", path, fileName, fileType);
        }
    }

    /**
     * 删除已消费文件
     */
    private void delLogFile() throws Exception {
        if (!isCanDelLog()) {
            logger.info("此文件还未消费,不予许删除,文件路径:{},文件名:{},文件类型:{}", path, fileName, fileType);
            return;
        }
        delFile();
    }

    /**
     * 删除已消费文件
     */
    private void delOffsetFile() throws Exception {
        if (!isCanDelOffset()) {
            logger.info("此文件还未消费,不予许删除,文件路径:{},文件名:{},文件类型:{}", path, fileName, fileType);
            return;
        }
        delFile();
    }

    private void delFile() {
        File file = new File(path + File.separator + String.format("%0" + MappedByteBufferUtil.NAME_LEN + "d", fileName) + fileType.name);
        if (file.exists()) {
            if (!file.delete()) {
                logger.warn("删除文件失败,文件路径:{},文件名:{},文件类型:{}", path, fileName, fileType);
            } else {
                logger.info("删除文件,文件路径:{},文件名:{},文件类型:{}", path, fileName, fileType);
            }
        } else {
            logger.info("文件不存在,文件路径:{},文件名:{},文件类型:{}", path, fileName, fileType);
        }
    }

    /**
     * 判断文件是否消费完
     * 如果都消费完就可以删除
     * @return true可以删除
     */
    private boolean isCanDelLog() throws Exception {
        long readOffset = fileName + fileQueue.getFileSize();
        return fileQueue.isReadLogComplete(readOffset);
    }

    /**
     * 判断文件是否消费完
     * 如果都消费完就可以删除
     * @return true可以删除
     */
    private boolean isCanDelOffset() throws Exception {
        long readOffset = fileName + fileQueue.getFileSize() / 8;
        return fileQueue.isReadOffsetComplete(readOffset);
    }
}
