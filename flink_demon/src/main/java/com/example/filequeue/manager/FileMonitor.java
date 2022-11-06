package com.example.filequeue.manager;

import com.example.filequeue.Task;
import com.example.filequeue.config.QueueConfig;
import com.example.filequeue.util.LogUtil;
import com.example.filequeue.util.SleepUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author wxy.
 */
public class FileMonitor implements Task {
    private final String filePath;
    private final ExecutorService monitorTask;
    private boolean stopped = false;
    private final static double WARN_SIZE = 20;
    /**
     * 10min
     */
    private final static long MONITOR_TIME = 10 * 60 * 1000;

    public FileMonitor() {
        this.filePath = QueueConfig.getInstance().getDataPath();

        //create a single thread
        this.monitorTask = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "FileMonitor-ReaderTask-");
            }
        });

    }

    private void monitor() {
        File file = new File(filePath);
        try {
            while (!stopped) {
                double fileSize = getDirSize(file);
                if (fileSize > WARN_SIZE) {
                    sendMessage("Permanent Queue file size is " + fileSize + "M, over Threshold " + WARN_SIZE);
                }
                SleepUtil.sleep(MONITOR_TIME);
            }
        } catch (Exception e) {
            LogUtil.error("monitor thread run error" + e.getCause());
        }

    }

    private double getDirSize(File file) {
        double size = 0;
        if (!file.exists()) {
            LogUtil.error("monitor dir is not exist");
            return size;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (null == children || children.length == 0) {
                return size;
            }
            for (File f : children) {
                size += getDirSize(f);
            }
            return size;
        } else {
            size = (double) file.length() / 1024 / 1024;
            return size;
        }

    }

    @Override
    public void start() {
        monitorTask.submit(new Runnable() {
            @Override
            public void run() {
                monitor();
            }
        });
    }

    @Override
    public void stop() {
        stopped = true;
        monitorTask.shutdown();
    }

    private void sendMessage(String message) {
        LogUtil.error(message);
    }
}
