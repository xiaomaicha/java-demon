package com.raymond.queue;

import com.raymond.queue.collection.CollectConsumption;
import com.raymond.queue.collection.CollectProduction;

import com.raymond.queue.utils.DateUtil;
import com.raymond.queue.utils.MappedByteBufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 队列
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-04 14:47
 */
@SuppressWarnings("all")
public class FileQueue<E> {
    private final static Logger logger = LoggerFactory.getLogger(FileQueue.class);
    /**
     * 文件绝对地址对应的文件队列
     * key:文件绝对地址
     * value:文件队列实例
     */
    private static final Map<String, FileQueue> TOPIC_MAP = new ConcurrentHashMap<>(16);

    private final ScheduledThreadPoolExecutor cleanPoolExecutor = FileQueue.javaScheduledThreadExecutor("cleanFileThread");

    public static String DEFAULT_PATH = System.getProperty("user.dir");
    public final boolean isCollect;

    /**
     * 当前队列的文件大小
     */
    private final long fileSize;

    final Map<String, Consumption<E>> groupMap = new ConcurrentHashMap<>(16);

    private final Class<E> eClass;

    private final String path;

    private final String topic;
    /**
     * 是否是普通队列
     */
    private boolean isOrdinary;

    private Production<E> production;

    private Consumption<E> consumption;

    public static final String DEFAULT_GROUP = "defaultGroup";

    /**
     * 创建普通队列模式
     * @param eClass 队列类型
     * @param topic 主题
     * @param <T> 泛型
     * @return 文件队列
     * @throws Exception 异常
     */
    public static <T> FileQueue<T> ordinary(Class<T> eClass, String topic) throws Exception {
        return instantiation(eClass, DEFAULT_PATH, topic, DEFAULT_GROUP, QueueModel.ORDINARY);
    }

    /**
     * 创建发布订阅模式
     * @param eClass 队列类型
     * @param topic 主题
     * @param <T> 泛型
     * @return 文件队列
     * @throws Exception 异常
     */
    public static <T> FileQueue<T> subscribe(Class<T> eClass, String topic, String groupName) throws Exception {
        return instantiation(eClass, DEFAULT_PATH, topic, groupName, QueueModel.SUBSCRIBE);
    }

    /**
     * 创建文件队列
     * @param eClass 类型
     * @param path 路径
     * @param topic 主题名称
     * @param groupName 消费组,如果是普通队列模式可以不用传
     * @param queueModel 队列模式
     * @param <T> 泛型
     * @return 文件对垒
     * @throws Exception 异常
     */
    public static <T> FileQueue<T> instantiation(Class<T> eClass, String path, String topic, String groupName, QueueModel queueModel) throws Exception {
        return instantiation(eClass, path, topic, groupName, queueModel, GrowMode.CONTINUE_OFFSET, MappedByteBufferUtil.FILE_SIZE);
    }

    /**
     * 创建文件队列
     * @param eClass 类型
     * @param path 路径
     * @param topic 主题名称
     * @param groupName 消费组,如果是普通队列模式可以不用传
     * @param queueModel 队列模式
     * @param growMode 消费组创建模式
     * @param type 1代表只生产, 2代表只消费, 3生产消费都支持
     * @param fileSize 文件大小
     * @param <T> 泛型
     * @return 文件对垒
     * @throws Exception 异常
     */
    public static <T> FileQueue<T> instantiation(Class<T> eClass, String path, String topic, String groupName,
                                                 QueueModel queueModel, GrowMode growMode, long fileSize) throws Exception {
        return instantiation(eClass, path, topic, groupName, queueModel, GrowMode.CONTINUE_OFFSET, MappedByteBufferUtil.FILE_SIZE, true);
    }
    /**
     * 创建文件队列
     * @param eClass 类型
     * @param path 路径
     * @param topic 主题名称
     * @param groupName 消费组,如果是普通队列模式可以不用传
     * @param queueModel 队列模式
     * @param growMode 消费组创建模式
     * @param type 1代表只生产, 2代表只消费, 3生产消费都支持
     * @param fileSize 文件大小
     * @param <T> 泛型
     * @return 文件对垒
     * @throws Exception 异常
     */
    public static <T> FileQueue<T> instantiation(Class<T> eClass, String path, String topic, String groupName,
                                                 QueueModel queueModel, GrowMode growMode, long fileSize, boolean isContinue) throws Exception {
        checkParam(path, topic, groupName, fileSize);
        if (growMode == GrowMode.COPY_GROUP) {
            throw new IllegalArgumentException("growMode Can't use COPY_GROUP");
        }
        File file = new File(path + File.separator + "queue");
        String key = file.getAbsolutePath() + ":" + topic;
        if (TOPIC_MAP.containsKey(key)) {
            return existence(key, eClass, topic, groupName, queueModel, growMode, isContinue);
        }
        synchronized (FileQueue.class) {
            if (TOPIC_MAP.containsKey(key)) {
                return existence(key, eClass, topic, groupName, queueModel, growMode, isContinue);
            }
            FileQueue<T> eFileQueue = new FileQueue<T>(eClass, file.getAbsolutePath(), topic, groupName, queueModel, growMode, fileSize, isContinue);
            TOPIC_MAP.put(key, eFileQueue);
            return eFileQueue;
        }
    }

    /**
     * 程序退出监听
     */
    private static void exit() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (FileQueue value : TOPIC_MAP.values()) {
                try {
                    value.close();
                } catch (Exception e) {
                    logger.error("程序退出关闭文件异常,topic:" + value.topic);
                }
            }
        }));
    }

    /**
     * 校验参数
     * @param path 文件路径
     * @param topic 主题
     * @param groupName 消费组,如果是普通队列模式可以不用传
     * @param type 1代表只生产, 2代表只消费, 3生产消费都支持
     * @param fileSize 文件大小
     */
    private static void checkParam(String path, String topic, String groupName, long fileSize) {
        if (MappedByteBufferUtil.isStrEmpty(path)) {
            throw new IllegalArgumentException("path is empty");
        }
        if (MappedByteBufferUtil.isStrEmpty(topic)) {
            throw new IllegalArgumentException("topic is empty");
        }
        if (MappedByteBufferUtil.isStrEmpty(groupName)) {
            throw new IllegalArgumentException("groupName is empty");
        }

        if (fileSize < 0 || fileSize > 1024 * 1024 * 1024) {
            throw new IllegalArgumentException("fileSize is argument,fileSize:" + fileSize);
        }
    }

    private static <T> FileQueue<T> existence(String key, Class<T> eClass, String topic, String groupName,
                                              QueueModel queueModel, GrowMode growMode, boolean isContinue) throws Exception {
        FileQueue<T> fileQueue = TOPIC_MAP.get(key);
        if (fileQueue.eClass != eClass) {
            throw new RuntimeException("和已有的类型不一致");
        }
        if (fileQueue.groupMap.containsKey(groupName)) {
            return fileQueue;
        }
        if (queueModel == QueueModel.ORDINARY) {
            throw new RuntimeException("普通队列模式不支持不同消费组");
        }
        fileQueue.createConsumption(groupName, growMode, "", isContinue);
        return fileQueue;
    }

    private FileQueue(Class<E> eClass, String path, String topic, String groupName, QueueModel queueModel, GrowMode growMode, long fileSize, boolean isContinue) throws Exception {
        logger.info("创建文件队列中, topic:{}, groupName:{}, 类型:{}, 文件大小:{}",
                topic, groupName, queueModel, fileSize);
        this.eClass = eClass;
        this.path = path;
        this.topic = topic;
        this.fileSize = fileSize;
        this.isOrdinary = queueModel == QueueModel.ORDINARY;
        this.isCollect = MappedByteBufferUtil.isCollection(eClass);
        initFile(topic);
        production = createProduction(this.path, topic, fileSize);
//        growMode = GrowMode.CONTINUE_OFFSET;
        if (MappedByteBufferUtil.isStrEmpty(groupName) && isOrdinary) {
            groupName = DEFAULT_GROUP;
        }
        Consumption<E> eConsumption = createConsumption(eClass, this.path, topic, groupName, growMode, "", fileSize, isContinue);
        groupMap.put(groupName, eConsumption);
        if (isOrdinary) {
            consumption = eConsumption;
        }
        cleanThread();
    }

    private Production<E> createProduction(String path, String topic, long fileSize) throws IOException {
        if (isCollect) {
            return new CollectProduction<>(path, topic, fileSize);
        }
        return new BlockingProduction<>(path, topic, fileSize);
    }

    private Consumption createConsumption(Class<E> eClass, String path, String topic, String groupName,
                                          FileQueue.GrowMode growMode, String srcGroupName, long fileSize, boolean isContinue) throws Exception {
        if (isCollect) {
            return new CollectConsumption(eClass, path, topic, groupName, this, growMode, srcGroupName, fileSize, isContinue);
        }
        return new BlockingConsumption(eClass, path, topic, groupName, this, growMode, "", fileSize, isContinue);
    }


    private void initFile(String topic) {
        String path = this.path + File.separator + topic;
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("文件目录创建失败");
        } else if (!file.isDirectory()) {
            throw new RuntimeException("文件目录创建失败");
        }
        if (!checkFilesize()) {
            throw new RuntimeException("文件大小与以前不一致，无法创建文件");
        }
    }

    protected boolean checkFilesize() {
        String path = this.path + File.separator + topic;
        File file = new File(path);
        File[] files = file.listFiles((dir, name) -> name.endsWith(FileQueue.FileType.LOG.name));
        if (files == null || files.length == 0) {
            return true;
        }
        return files[0].length() == MappedByteBufferUtil.FILE_SIZE;
    }


    /**
     * 复制消费组
     * 消费组存在的话继续上次消费,消费组不存在的话,复制来源消费组
     * @param groupName 需要创建的消费组
     * @param srcGroupName 来源消费组
     * @return 新的消费组
     * @throws Exception 异常
     */
    public Consumption<E> copyGroup(String groupName, String srcGroupName) throws Exception {
        return copyGroup(groupName, srcGroupName, true);
    }



    /**
     * 复制消费组
     * @param groupName 需要创建的消费组
     * @param srcGroupName 来源消费组
     * @param isContinue
     * true:消费组存在的话继续上次消费,消费组不存在的话,复制来源消费组
     * false:不管消费组是否存在,都复制来源消费组
     * @return 新的消费组
     * @throws Exception 异常
     */
    public Consumption<E> copyGroup(String groupName, String srcGroupName, boolean isContinue) throws Exception {
        return createGroup(groupName, GrowMode.COPY_GROUP, srcGroupName, isContinue);
    }

    public Consumption<E> createFirstGroup(String groupName, boolean isContinue) throws Exception {
        return createGroup(groupName, GrowMode.FIRST_OFFSET, "", isContinue);
    }


    /**
     * 创建消费组
     *
     * @param groupName 消费组名称
     * @param isContinue
     * true:消费组存在的话继续上次消费,消费组不存在的话,从最后一条开始消费
     * false:不管消费组是否存在,都从最后一条开始消费
     * @return 返回消费者
     * @throws Exception 异常
     */
    public Consumption<E> createLastGroup(String groupName, boolean isContinue) throws Exception {
        return createGroup(groupName, GrowMode.LAST_OFFSET, "", isContinue);
    }

    /**
     * 创建消费组
     * @param groupName 消费组名称
     * @param growMode 创建消费组的模式
     * @return 消费组对象
     * @throws Exception 异常
     */
    public Consumption<E> createGroup(String groupName, GrowMode growMode) throws Exception {
        return createGroup(groupName, growMode, "", true);
    }

    /**
     * 创建消费组
     * @param groupName 消费组名称
     * @param growMode 创建消费组的模式
     * @return 消费组对象
     * @throws Exception 异常
     */
    public Consumption<E> createGroup(String groupName, GrowMode growMode, boolean isContinue) throws Exception {
        return createGroup(groupName, growMode, "", isContinue);
    }

    public Consumption<E> createGroup(String groupName, GrowMode growMode, String srcGroupName, boolean isContinue) throws Exception {
        if (isOrdinary) {
            throw new RuntimeException("普通队列模式不支持不同消费组");
        }
        if (groupMap.containsKey(groupName)) {
            return groupMap.get(groupName);
        }
        if (isContinue && existsGroup(groupName)) {
            growMode = GrowMode.CONTINUE_OFFSET;
        }
        return createConsumption(groupName, growMode, srcGroupName, isContinue);
    }

    private Consumption<E> createConsumption(String groupName, GrowMode growMode, String srcGroupName, boolean isContinue) throws Exception {
        synchronized (this) {
            if (groupMap.containsKey(groupName)) {
                return groupMap.get(groupName);
            }
            Consumption<E> eConsumption = createConsumption(eClass, this.path, topic, groupName, growMode, srcGroupName, fileSize, isContinue);
            groupMap.put(groupName, eConsumption);
            return eConsumption;
        }
    }

    public Production<E> getProduction() {
        return this.production;
    }


    public Consumption<E> getConsumption() {
        if (!isOrdinary) {
            throw new RuntimeException("请传入消费组的名称");
        }
        return consumption;
    }

    public Consumption<E> getConsumption(String groupName) throws Exception {
        Consumption<E> eConsumption = groupMap.get(groupName);
        if (eConsumption == null) {
            if (existsGroup(groupName)) {
                return createGroup(groupName, GrowMode.CONTINUE_OFFSET);
            } else {
                throw new NullPointerException("当前消费组不存在,请先创建消费组");
            }
        }
        return eConsumption;
    }

    public List<Consumption> getConsumptions() {
        return new ArrayList<>(groupMap.values());
    }

    /**
     * 判断消费组是否存在
     * @param groupName 消费组名称
     * @return true 存在
     */
    public boolean existsGroup(String groupName) {
        if (MappedByteBufferUtil.isStrEmpty(groupName)) {
            return false;
        }
        File file = new File(this.path + File.separator + topic + File.separator + groupName + FileType.READ.name);
        return file.exists() && file.isFile();
    }

    public boolean existsTopic() {
        File file = new File(this.path + File.separator + topic + File.separator + "queue" + FileType.WRITE.name);
        return file.exists() && file.isFile();
    }

    public boolean add(E e) {
        put(e);
        return true;
    }

    public E poll(String groupName) throws Exception {
        Consumption<E> eConsumption = groupMap.get(groupName);
        if (eConsumption != null) {
            return eConsumption.poll();
        }
        if (!existsGroup(groupName)) {
            throw new NullPointerException("当前消费组不存在:" + groupName);
        }
        return createGroup(groupName, GrowMode.CONTINUE_OFFSET).poll();
    }


    public E poll() {
        if (!isOrdinary) {
            throw new RuntimeException("请传入消费组的名称");
        }
        return consumption.poll();
    }

    public void put(E e) {
        production.put(e);
    }

    public E take() throws InterruptedException {
        return ((BlockingConsumption<E>)consumption).take();
    }

    public int size() {
        if (!isOrdinary) {
            throw new RuntimeException("请传入消费组的名称");
        }
        return (int)(production.getWriteIndex() - consumption.getReadIndex());
    }

    public int size(String groupName) throws Exception {
        Consumption<E> eConsumption = groupMap.get(groupName);
        if (eConsumption != null) {
            return (int)(production.getWriteIndex() - eConsumption.getReadIndex());
        }
        if (!existsGroup(groupName)) {
            throw new RuntimeException("当前消费组不存在:" + groupName);
        }
        return (int)(production.getWriteIndex() - createGroup(groupName, GrowMode.CONTINUE_OFFSET).getReadIndex());
    }


    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isEmpty(String groupName) throws Exception {
        return size(groupName) == 0;
    }



    public List<E> list(int count) {
        List<E> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            E poll = poll();
            if (poll == null) {
                return list;
            }
            list.add(poll);
        }
        return list;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void close() throws IOException {
        production.close();
        for (Consumption<E> value : groupMap.values()) {
            value.close();
        }
    }

    public boolean isComplete(long readValue, FileQueue.FileType fileType) throws Exception {
        if (fileType == FileQueue.FileType.LOG) {
            return isReadLogComplete(readValue);
        }
        if (fileType == FileQueue.FileType.OFFSET_LIST) {
            return isReadOffsetComplete(readValue);
        }
        throw new RuntimeException("文件类型不能判断,:fileType" + fileType);
    }

    /**
     * 判断log文件是否读完
     * @param readOffset
     * @return
     * @throws Exception
     */
    public synchronized boolean isReadLogComplete(long readOffset) throws Exception {
        for (Consumption consumption : groupMap.values()) {
            if (consumption.readOffset <= readOffset) {
                logger.warn("未完成消费,topic:{},groupName:{},需要删除的readOffset:{},未消费的readOffset:{}",
                        topic, consumption.getGroupName(), readOffset, consumption.readOffset);
                return false;
            }
        }
        List<Consumption<E>> notInitConsumptions = getNotInitConsumptions();
        try {
            for (Consumption<E> notInitConsumption : notInitConsumptions) {
                if (notInitConsumption.readOffset <= readOffset) {
                    logger.warn("消费组未启动,未完成消费,topic:{},groupName:{},需要删除的readOffset:{},未消费的readOffset:{}",
                            topic, notInitConsumption.getGroupName(), readOffset, notInitConsumption.readOffset);
                    return false;
                }
            }
        } finally {
            close(notInitConsumptions);
        }
        return true;
    }

    /**
     * 判断offset文件是否读完
     * @param readIndex
     * @return
     * @throws Exception
     */
    public synchronized boolean isReadOffsetComplete(long readIndex) throws Exception {
        for (Consumption consumption : groupMap.values()) {
            if (consumption.readIndex <= readIndex) {
                logger.warn("未完成消费,topic:{},groupName:{},需要删除的readIndex:{},未消费的readIndex:{}",
                        topic, consumption.getGroupName(), readIndex, consumption.readIndex);
                return false;
            }
        }
        List<Consumption<E>> notInitConsumptions = getNotInitConsumptions();
        try {
            for (Consumption<E> notInitConsumption : notInitConsumptions) {
                if (notInitConsumption.readIndex <= readIndex) {
                    logger.warn("消费组未启动,未完成消费,topic:{},groupName:{},需要删除的readIndex:{},未消费的readIndex:{}",
                            topic, notInitConsumption.getGroupName(), readIndex, notInitConsumption.readIndex);
                    return false;
                }
            }
        } finally {
            close(notInitConsumptions);
        }
        return true;
    }

    /**
     * 获取未初始化的消费组
     * @return 为初始化的消费组
     * @throws Exception 异常
     */
    private List<Consumption<E>> getNotInitConsumptions() throws Exception {
        List<Consumption<E>> consumptions = new ArrayList<>();
        List<String> readFile = getReadFile();
        for (String s : groupMap.keySet()) {
            readFile.remove(s);
        }
        if (readFile.size() == 0) {
            return consumptions;
        }
        for (String groupName : readFile) {
            consumptions.add(createConsumption(eClass, this.path, topic, groupName, GrowMode.CONTINUE_OFFSET, "", fileSize, true));
        }
        return consumptions;
    }

    private void close(List<Consumption<E>> notInitConsumptions) {
        if (notInitConsumptions == null) {
            return;
        }
        for (Consumption<E> notInitConsumption : notInitConsumptions) {
            try {
                notInitConsumption.close();
            } catch (IOException e) {
                logger.error("关闭消费组异常,主题:{},消费组:{}", topic, notInitConsumption.getGroupName());
            }
        }
    }
    /**
     * 获取所有消费组
     * @return
     */
    private List<String> getReadFile() {
        File[] files = new File(this.path + File.separator + this.topic).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(FileQueue.FileType.READ.name);
            }
        });
        if (files == null || files.length < 1) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName().replace(FileQueue.FileType.READ.name, ""));
        }
        return names;
    }

    /**
     * 清理文件线程
     */
    private void cleanThread() {
        int period = 24 * 60 * 60 * 1000;
        Date offset = DateUtil.offset(new Date(), 1);
        try {
            Date parse = DateUtil.parse(DateUtil.dateToStr(offset, DateUtil.DateStyle.YYYY_MM_DD) + " 03:00:00");
            long initialDelay = parse.getTime() - System.currentTimeMillis();
            cleanPoolExecutor.scheduleAtFixedRate(this::cleanFile, 10, 10 * 1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("定时清理文件线程异常:", e);
        }
    }

    private void cleanFile() {
        try {
            cleanFileByType(FileQueue.FileType.LOG);
            cleanFileByType(FileQueue.FileType.OFFSET_LIST);
        } catch (Exception e) {
            logger.error("清除线程异常:", e);
        }
    }

    private void cleanFileByType(FileQueue.FileType fileType) throws Exception {
        String path = this.path + File.separator + topic;
        File file = new File(path);
        File[] files = file.listFiles((dir, name) -> name.endsWith(fileType.name));
        int fileNumber = 1;
        if (files == null || files.length < fileNumber) {
            return;
        }
        Arrays.sort(files, Comparator.comparingLong(f -> Long.parseLong(f.getName().substring(0, MappedByteBufferUtil.NAME_LEN))));
        for (int i = 0; i < files.length - fileNumber; i++) {
            long read = Long.parseLong(files[i].getName().substring(0, MappedByteBufferUtil.NAME_LEN));
            if (!isComplete(read, fileType)) {
                return;
            }
            new CleanFile(this, path, read, fileType).delCompleteFile();
        }
    }

    protected static ScheduledThreadPoolExecutor javaScheduledThreadExecutor(String threadName) {
        return new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setName(threadName);
            thread.setDaemon(true);
            return thread;
        });
    }

    static ThreadPoolExecutor javaThreadExecutor(String threadName) {
        return new ThreadPoolExecutor(1, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), new ThreadFactory() {
            private AtomicInteger id = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(threadName + "-" + id);
                thread.setDaemon(true);
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }


    public enum FileType {
        /**
         * 日志文件
         */
        LOG(".log"),
        /**
         * 偏移量文件(索引文件)
         */
        OFFSET_LIST(".offsetList"),
        /**
         * 写文件
         */
        WRITE(".write"),
        /**
         * 读文件
         */
        READ(".read");

        FileType(String name) {
            this.name = name;
        }

        public String name;
    }

    public enum QueueModel {
        /**
         * 普通队列
         * 在多进程下无法会改成发布订阅
         */
        ORDINARY,
        /**
         * 发布订阅
         */
        SUBSCRIBE;
    }

    public enum GrowMode {
        /**
         * 从当前最后一条开始
         */
        LAST_OFFSET,
        /**
         * 从某个消费组开始
         */
        COPY_GROUP,
        /**
         * 从上次消费的开始,如果不存在上次消费,则按最后一条开始
         */
        CONTINUE_OFFSET,
        /**
         * 从首条开始
         */
        FIRST_OFFSET;
    }

}
