package com.example.flink;
/*
 * @Classname Goods
 * @Description
 * @Date 2022/9/7 23:41
 * @author by dell
 */


import java.io.Serializable;
import java.util.Date;

public class Goods implements Serializable {

    private String goodsId;
    private String goodsName;
    private int goodsPrice;
    private Date date;

    private String message;

    public Goods() {
        this.message = "稀土掘金\n" +
                "首页\n" +
                "沸点\n" +
                "课程\n" +
                "直播\n" +
                "活动\n" +
                "商城\n" +
                "APP\n" +
                "插件\n" +
                "\n" +
                "探索稀土掘金\n" +
                "搜索\n" +
                "创作者中心\n" +
                "首次成功发布 400 字以上文章\n" +
                "\n" +
                "\n" +
                "\n" +
                "vip\n" +
                "会员\n" +
                "6\n" +
                "琦。。的头像\n" +
                "Java文件的简单读写、随机读写、NIO读写与使用MappedByteBuffer读写\n" +
                "\n" +
                "Java艺术\n" +
                "lv-4\n" +
                "2021年02月26日 23:30 ·  阅读 1896\n" +
                "关注\n" +
                "本篇内容包括：\n" +
                "\n" +
                "面向文件编程的重要性\n" +
                "简单文件读写\n" +
                "随机访问文件读写\n" +
                "NIO文件读写-FileChannel\n" +
                "使用MappedByteBuffer读写文件\n" +
                "面向文件编程的重要性\n" +
                "在我印象中，似乎很少有关于文件操作的面试题，而大多数面试题都围绕着高并发、网络编程、RPC、数据库，但其实掌握文件操作也同等重要。只是我们很少会碰到需要操作文件的需求，毕竟百分之九十的工作都是依靠操作数据库、网络通信完成，而存储都被各类关系型数据库、分布式数据库、缓存、搜索引擎、甚至云存储替代了。\n" +
                "\n" +
                "虽然偶尔我们也需要实现文件上传的接口，但文件上传一般都会选择存储到云端，顶多就临时转存一下，相信很多人会选择直接百度拷贝一份代码完成文件存储了事，甚至于都不关心是如何实现的。而多数的表格导出操作也都依赖一些现成的框架，以致于面向文件编程的重要性被弱化了。\n" +
                "\n" +
                "如果我们去研究一些框架的底层源码，我们就能发现掌握文件操作其实也很重要。以RocketMQ为例，RocketMQ的消息存储并没有借用数据库，也没有借用其它第三方框架，仅仅是用文件存储。我很好奇，为什么没有面试题问RocketMQ的消息存储实现原理。\n" +
                "\n" +
                "我自己也开发过一些组件/框架/中间件，但由于文件操作这块知识太欠缺，首先想到的都是依赖一些第三方存储中间件/库实现，如Redis、Mysql、LevelDB，这直接提升了框架的使用成本。所以我也一直知道掌握文件操作的重要性。\n" +
                "\n" +
                "有时候我也在想，为什么部署Kafka（旧版本）要部署一个Zookeeper，而部署Zookeeper的作用只是用于管理节点、消费者、实现Leader选举。部署Zookeeper为了保证Zookeeper的可用性又要部署几个节点，这无疑增加了Kafka的使用成本。所以当我看到Alibaba Sentinel实现集群限流功能提供嵌入式模式时就很理解为什么要同时提供嵌入式部署和独立部署两种模式。\n" +
                "\n" +
                "我去年开始着手自研一个分布式延迟调度中间件，其实核心功能早就实现了，也以嵌入式部署的方式在项目中支撑业务功能。但为了去掉依赖Redis实现存储功能、第三方框架实现RPC功能、广播机制实现Leader选举功能，我才决定重新写一个。因此我用Raft共识算法+LevelDB（Key-Value存储库）替代Redis实现存储、基于Netty自己封装RPC框架、基于Raft算法替代广播实现Leader选举。这直接就降低了这款自研中间件的使用成本。而在实现Raft算法的日记Appender时，我又遇到了同样的槛，但这次我选择跨过去。\n" +
                "\n" +
                "阿里开源的众多项目中，除RocketMQ的消息存储使用文件存储外，Sentinel存储资源指标数据统计也是使用文件存储，这两个框架在实现存储上都使用了同一种设计思想，即数据文件+索引文件。我在自研分布式延迟调度中间件中就借鉴了RocketMQ与Sentinel中的文件存储索引设计，数据文件存储日记，而索引文件存储日记ID与日记在数据文件中的物理偏移量。\n" +
                "\n" +
                "Sentinel按精确到秒的时间戳存储索引，和时间戳是有序增长的，而且时间戳是long类型占8个字节，根据单个指标文件的最大大小，物理偏移量也正好可以是long类型，因此每个索引占16个字节。资源指标数据可能由于某段时间没有请求或者应用重启导致某些时间戳没有记录，但至少时间戳是单调递增的，因此我们只需要采用简单的折半查找就能快速定位到索引。由于Sentinel资源指标数据收集不需要考虑高并发，这样的设计足以满足需求。\n" +
                "\n" +
                "RocketMQ需要提供可以通过key或时间区间来查询消息的功能，因此RocketMQ的索引存储实现相对Sentinel较难。单个索引文件固定的文件大小约为400M，一个索引文件可以保存2000W个索引，索引文件的底层存储设计相当于是在文件系统中实现HashMap结构，每个文件头存储了此文件存储的消息的最小时间戳和最大时间戳，这用于实现按时间区间搜索消息记录。消息key的hash值则作为索引项存放在索引文件中的物理偏移量，当然，还要加上文件头的大小，以及乘以单项索引占用的字节数。\n" +
                "\n" +
                "你或许觉得，RocketMQ与Sentinel实现索引难在算法，的确，算法是灵魂。但软件的强大依然需要依赖硬件的支持，你是否考虑到，如何跳转到文件中的某个位置读取指定字节的数据，又如何改写文件中指定位置的数据？如何考虑并发读写问题，如何调优性能？文件的NIO如何理解、如何使用MappedByteBuffer提升性能以及原理是什么？\n" +
                "\n" +
                "解读这几个问题是我学习文件读写的目的，也体现了掌握面向文件编程的重要性。虽然为了提升工作效率以及降低犯错率我们并不需要重复造轮子，但重复造轮子无疑是提升自身能力最高效的学习方法。\n" +
                "\n" +
                "简单文件读写\n" +
                "由于文件与目录的创建和删除较为简单，因此忽略这部分内容的介绍，我们重点学习文件的读写。\n" +
                "\n" +
                "FileOutputStream\n" +
                "由于流是单向的，简单文件写可使用FileOutputStream，而读文件则使用FileInputStream。\n" +
                "\n" +
                "任何数据输出到文件都是以字节为单位输出，包括图片、音频、视频。以图片为例，如果没有图片格式解析器，那么图片文件其实存储的就只是按某种格式存储的字节数据罢了。\n" +
                "\n" +
                "FileOutputStream指文件字节输出流，用于将字节数据输出到文件，仅支持顺序写入、支持以追加方式写入，但不支持在指定位置写入。\n" +
                "\n" +
                "打开一个文件输出流并写入数据的示例代码如下。\n" +
                "\n" +
                "public class FileOutputStreamStu{\n" +
                "    public void testWrite(byte[] data) throws IOException {                    \n" +
                "        try(FileOutputStream fos = new FileOutputStream(\"/tmp/test.file\",true)) {\n" +
                "            fos.write(data);\n" +
                "            fos.flush();\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "注意，如果不指定追加方式打开流，new FileOutputStream时会导致文件内容被清空，而FileOutputStream的默认构建函数是以非追加模式打开流的。\n" +
                "\n" +
                "FileOutputStream的参数1为文件名，参数2为是否以追加模式打开流，如果为true，则字节将写入文件的尾部而不是开头。\n" +
                "\n" +
                "调用flush方法目的是在流关闭之前清空缓冲区数据，实际上使用FileOutputStream并不需要调用flush方法，此处的刷盘指的是将缓存在JVM内存中的数据调用系统函数write写入。如BufferedOutputStream，在调用BufferedOutputStream方法时，如果缓存未满，实际上是不会调用系统函数write的，如下代码所示。\n" +
                "\n" +
                "public class BufferedOutputStream extends FilterOutputStream {\n" +
                "    public synchronized void write(byte b[], int off, int len) throws IOException {\n" +
                "        if (len >= buf.length) {\n" +
                "            flushBuffer();\n" +
                "            out.write(b, off, len);\n" +
                "            return;\n" +
                "        }\n" +
                "        if (len > buf.length - count) {\n" +
                "            flushBuffer();\n" +
                "        }\n" +
                "        System.arraycopy(b, off, buf, count, len); // 只写入缓存\n" +
                "        count += len;\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "FileInputStream\n" +
                "FileInputStream指文件字节输入流，用于将文件中的字节数据读取到内存中，仅支持顺序读取，不可跳跃读取。\n" +
                "\n" +
                "打开一个文件输入流读取数据的案例代码如下。\n" +
                "\n" +
                "public class FileInputStreamStu{\n" +
                "    public void testRead() throws IOException {    \n" +
                "    \ttry (FileInputStream fis = new FileInputStream(\"/tmp/test/test.log\")) {\n" +
                "        \tbyte[] buf = new byte[1024];\n" +
                "        \tint realReadLength = fis.read(buf);\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "其中buf数组中下标从0到realReadLength的字节数据就是实际读取的数据，如果realReadLength返回-1，则说明已经读取到文件尾并且未读取到任何数据。\n" +
                "\n" +
                "当然，我们还可以一个字节一个字节的读取，如下代码所示。\n" +
                "\n" +
                "public class FileInputStreamStu{\n" +
                "    public void testRead() throws IOException {     \n" +
                "        try (FileInputStream fis = new FileInputStream(\"/tmp/test/test.log\")) {\n" +
                "            int byteData = fis.read(); // 返回值取值范围：[-1,255]\n" +
                "            if (byteData == -1) {\n" +
                "                return; // 读取到文件尾了\n" +
                "            }\n" +
                "            byte data = (byte) byteData;\n" +
                "            // data为读取到的字节数据\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "至于读取到的字节数据如何使用就需要看你文件中存储的是什么数据了。\n" +
                "\n" +
                "如果整个文件存储的是一张图片，那么需要将整个文件读取完，再按格式解析成图片，而如果整个文件是配置文件，则可以一行一行读取，遇到\\n换行符则为一行，代码如下。\n" +
                "\n" +
                "public class FileInputStreamStu{\n" +
                "    @Test\n" +
                "    public void testRead() throws IOException {\n" +
                "        try (FileInputStream fis = new FileInputStream(\"/tmp/test/test.log\")) {\n" +
                "            ByteBuffer buffer = ByteBuffer.allocate(1024);\n" +
                "            int byteData;\n" +
                "            while ((byteData = fis.read()) != -1) {\n" +
                "                if (byteData == '\\n') {\n" +
                "                    buffer.flip();\n" +
                "                    String line = new String(buffer.array(), buffer.position(), buffer.limit());\n" +
                "                    System.out.println(line);\n" +
                "                    buffer.clear();\n" +
                "                    continue;\n" +
                "                }\n" +
                "                buffer.put((byte) byteData);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "Java基于InputStream、OutputStream还提供了很多的API方便读写文件，如BufferedReader，但如果懒得去记这些API的话，只需要记住FileInputStream与FileOutputStream就够了。\n" +
                "\n" +
                "随机访问文件读写\n" +
                "RandomAccessFile相当于是FileInputStream与FileOutputStream的封装结合，即可以读也可以写，并且RandomAccessFile支持移动到文件指定位置处开始读或写。\n" +
                "\n" +
                "RandomAccessFile的使用如下。\n" +
                "\n" +
                "public class RandomAccessFileStu{\n" +
                "    public void testRandomWrite(long index,long offset){\n" +
                "        try (RandomAccessFile randomAccessFile = new RandomAccessFile(\"/tmp/test.idx\", \"rw\")) {\n" +
                "            randomAccessFile.seek(index * indexLength());\n" +
                "            randomAccessFile.write(toByte(index));\n" +
                "            randomAccessFile.write(toByte(offset));\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "RandomAccessFile构建方法：参数1为文件路径，参数2为模式，'r'为读，'w'为写；\n" +
                "seek方法：在linux、unix操作系统下就是调用系统的lseek函数。\n" +
                "RandomAccessFile的seek方法通过调用native方法实现，源码如下。\n" +
                "\n" +
                "JNIEXPORT void JNICALL\n" +
                "Java_java_io_RandomAccessFile_seek0(JNIEnv *env,\n" +
                "                    jobject this, jlong pos) {\n" +
                "    FD fd;\n" +
                "    fd = GET_FD(this, raf_fd);\n" +
                "    if (fd == -1) {\n" +
                "        JNU_ThrowIOException(env, \"Stream Closed\");\n" +
                "        return;\n" +
                "    }\n" +
                "    if (pos < jlong_zero) {\n" +
                "        JNU_ThrowIOException(env, \"Negative seek offset\");\n" +
                "    }\n" +
                "    // #define IO_Lseek lseek\n" +
                "    else if (IO_Lseek(fd, pos, SEEK_SET) == -1) {\n" +
                "        JNU_ThrowIOExceptionWithLastError(env, \"Seek failed\");\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "Java_java_io_RandomAccessFile_seek0函数的参数1表示RandomAccessFile对象，参数2表示偏移量。函数中调用的IO_Lseek方法实际是操作系统的lseek方法。\n" +
                "\n" +
                "RandomAccessFile提供的读、写、指定偏移量其实都是通过调用操作系统函数完成的，包括前面介绍的文件输入流和文件输出流也不例外。\n" +
                "\n" +
                "NIO文件读写-FileChannel\n" +
                "Channel（通道）表示IO源与目标打开的连接，Channel类似于传统的流，但Channel本身不能直接访问数据，只能与Buffer进行交互。Channel（通道）主要用于传输数据，从缓冲区的一侧传到另一侧的实体（如File、Socket），支持双向传递。\n" +
                "\n" +
                "正如SocketChannel是客户端与服务端通信的通道，FileChannel就是我们读写文件的通道。FileChannel是线程安全的，也就是一个FileChannel可以被多个线程使用。对于多线程操作，同时只会有一个线程能对该通道所在文件进行修改。如果需要确保多线程的写入顺序，就必须要转为队列写入。\n" +
                "\n" +
                "FileChannel可通过FileOutputStream、FileInputStream、RandomAccessFile获取，也可以通过FileChannel#open方法打开一个通道。\n" +
                "\n" +
                "以通过FileOutputStream获取FileChannel为例，通过FileOutputStream或RandomAccessFile获取FileChannel方法相同，代码如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void testGetFileCahnnel(){\n" +
                "        try(FileOutputStream fos = new FileOutputStream(\"/tmp/test.log\");\n" +
                "            FileChannel fileChannel = fos.getChannel()){\n" +
                "           // do....   \n" +
                "        }catch (IOException exception){\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "需要注意，通过FileOutputStream获取的FileChannel只能执行写操作，通过FileInputStream获取的FileChannel只能执行读操作，原因可查看getChannel方法源码。\n" +
                "\n" +
                "通过FileOutputStream或FileInputStream或RandomAccessFile打开的FileChannel，在流关闭时也会被关闭，可查看这几个类的close方法源码。\n" +
                "\n" +
                "若想要获取一个同时支持读和写的FileChannel需要通过open方法打开，代码如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void testOpenFileCahnnel(){\n" +
                "        FileChannel channel = FileChannel.open(\n" +
                "                            Paths.get(URI.create(\"file:\" + rootPath + \"/\" + postion.fileName)),\n" +
                "                            StandardOpenOption.READ,StandardOpenOption.WRITE);\n" +
                "        // do....\n" +
                "        channel.close();\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "open方法第二个变长参数传StandardOpenOption.READ和StandardOpenOption.WRITE即可打开一个双向读写的通道。\n" +
                "\n" +
                "FileChannel允许对文件加锁，文件锁是进程级别的，不是线程级别的，文件锁可以解决多个进程并发访问、修改同一个文件的问题。文件锁会被当前进程持有，一旦获取到文件锁就要调用一次release释放锁，当关闭对应的FileChannel对象时或当前JVM进程退出时，锁也会自动被释锁。\n" +
                "\n" +
                "文件锁的使用案例代码如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void testFileLock(){\n" +
                "        FileChannel channel = this.channel;\n" +
                "        FileLock fileLock = null;\n" +
                "        try {\n" +
                "            fileLock = channel.lock();// 获取文件锁\n" +
                "            // 执行写操作\n" +
                "            channel.write(...);\n" +
                "            channel.write(...);\n" +
                "        } finally {\n" +
                "            if (fileLock != null) {\n" +
                "                fileLock.release(); // 释放文件锁\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "当然，只要我们能确保同时只有一个进程对文件执行写操作，那么就不需要锁文件。RocketMQ也并没有使用文件锁，因为每个Broker有自己数据目录，即使一台机器上部署多个Broker也不会有多个进程对同一个日记文件操作的情况。\n" +
                "\n" +
                "上面例子去掉文件锁后代码如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void testWrite(){\n" +
                "        FileChannel channel = this.channel;\n" +
                "        channel.write(...);\n" +
                "        channel.write(...);\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "这里还存在一个问题，就是并发写数据问题。虽然FileChannel是线程安全的，但两次write并不是原子性操作，如果要确保两次write是连续写入的，还必须要加锁。在RocketMQ中，通过引用计数器替代了锁。\n" +
                "\n" +
                "FileChannel提供的force方法用于刷盘，即调用操作系统的fsync函数，使用如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void closeChannel(){\n" +
                "        this.channel.force(true);\n" +
                "        this.channel.close();\n" +
                "    }        \n" +
                "}\n" +
                "复制代码\n" +
                "force方法的参数表示除强制写入内容更改外，文件元数据的更改是否也强制写入。后面使用MappedByteBuffer时，可直接使用MappedByteBuffer的force方法。\n" +
                "\n" +
                "FileChannel的force方法最终调用的C方法源码如下：\n" +
                "\n" +
                "JNIEXPORT jint JNICALL\n" +
                "Java_sun_nio_ch_FileDispatcherImpl_force0(JNIEnv *env, jobject this,\n" +
                "                                          jobject fdo, jboolean md)\n" +
                "{\n" +
                "    jint fd = fdval(env, fdo);\n" +
                "    int result = 0;\n" +
                "    if (md == JNI_FALSE) {\n" +
                "        result = fdatasync(fd);\n" +
                "    } else {\n" +
                "        result = fsync(fd);\n" +
                "    }\n" +
                "    return handle(env, result, \"Force failed\");\n" +
                "}\n" +
                "复制代码\n" +
                "参数md对应调用force方法传递的metaData参数。\n" +
                "\n" +
                "使用FileChannel支持seek（position）到指定位置读或写数据，代码如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void testSeekWrite(){\n" +
                "        FileChannel channel = this.channel;\n" +
                "        synchronized (channel) { \n" +
                "            channel.position(100);\n" +
                "            channel.write(ByteBuffer.wrap(toByte(index)));\n" +
                "            channel.write(ByteBuffer.wrap(toByte(offset)));\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "上述例子的作用是将指针移动到物理偏移量100byte位置处，顺序写入index和offset。读取同理，代码如下。\n" +
                "\n" +
                "public class FileChannelStu{\n" +
                "    public void testSeekRead(){\n" +
                "        FileChannel channel = this.channel;\n" +
                "        synchronized (channel) { \n" +
                "            channel.position(100);\n" +
                "            ByteBuffer buffer = ByteBuffer.allocate(16);\n" +
                "            int realReadLength = channel.read(buffer); \n" +
                "            if(realReadLength==16){\n" +
                "                long index = buffer.getLong();\n" +
                "                long offset = buffer.getLong();\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "其中read方法返回的是实际读取的字节数，如果返回-1则代表已经是文件尾部了，没有剩余内容可读取。\n" +
                "\n" +
                "使用MappedByteBuffer读写文件\n" +
                "MappedByteBuffer是Java提供的基于操作系统虚拟内存映射（MMAP）技术的文件读写API，底层不再通过read、write、seek等系统调用实现文件的读写。\n" +
                "\n" +
                "我们需要通过FileChannel#map方法将文件的一个区域映射到内存中，代码如下。\n" +
                "\n" +
                "public class MappedByteBufferStu{\n" +
                "  @Test\n" +
                "  public void testMappedByteBuffer() throws IOException {\n" +
                "      FileChannel fileChannel = FileChannel.open(Paths.get(URI.create(\"file:/tmp/test/test.log\")),\n" +
                "                StandardOpenOption.WRITE, StandardOpenOption.READ);\n" +
                "      MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);\n" +
                "      fileChannel.close();\n" +
                "      mappedByteBuffer.position(1024);\n" +
                "      mappedByteBuffer.putLong(10000L);\n" +
                "      mappedByteBuffer.force();    \n" +
                "  }\n" +
                "}\n" +
                "复制代码\n" +
                "上面代码的功能是通过FileChannel将文件[0~4096)区域映射到内存中，调用FileChannel的map方法返回MappedByteBuffer，在映射之后关闭通道，随后在指定位置处写入一个8字节的long类型整数，最后调用force方法将写入数据从内存写回磁盘（刷盘）。\n" +
                "\n" +
                "映射一旦建立了，就不依赖于用于创建它的文件通道，因此在创建MappedByteBuffer之后我们就可以关闭通道了，对映射的有效性没有影响。\n" +
                "\n" +
                "实际上将文件映射到内存比通过read、write系统调用方法读取或写入几十KB的数据要昂贵，从性能的角度来看，MappedByteBuffer适合用于将大文件映射到内存中，如上百M、上GB的大文件。\n" +
                "\n" +
                "FileChannel的map方法有三个参数：\n" +
                "\n" +
                "MapMode：映射模式，可取值有READ_ONLY（只读映射）、READ_WRITE（读写映射）、PRIVATE（私有映射），READ_ONLY只支持读，READ_WRITE支持读写，而PRIVATE只支持在内存中修改，不会写回磁盘；\n" +
                "position和size：映射区域，可以是整个文件，也可以是文件的某一部分，单位为字节。\n" +
                "需要注意的是，如果FileChannel是只读模式，那么map方法的映射模式就不能指定为READ_WRITE。如果文件是刚刚创建的，只要映射成功，文件的大小就会变成（0+position+size）。\n" +
                "\n" +
                "通过MappedByteBuffer读取数据示例如下：\n" +
                "\n" +
                "public class MappedByteBufferStu{\n" +
                "    @Test\n" +
                "    public void testMappedByteBufferOnlyRead() throws IOException {\n" +
                "        FileChannel fileChannel = FileChannel.open(Paths.get(URI.create(\"file:/tmp/test/test.log\")),\n" +
                "                    StandardOpenOption.READ);\n" +
                "        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4096);\n" +
                "        fileChannel.close();\n" +
                "        mappedByteBuffer.position(1024);\n" +
                "        long value = mappedByteBuffer.getLong();\n" +
                "        System.out.println(value);\n" +
                "    }\n" +
                "}\n" +
                "复制代码\n" +
                "mmap绕过了read、write系统函数调用，绕过了一次数据从内核空间到用户空间的拷贝，即实现零拷贝，MappedByteBuffer使用直接内存而非JVM的堆内存。\n" +
                "\n" +
                "mmap只是在虚拟内存分配了地址空间，只有在第一次访问虚拟内存的时候才分配物理内存。在mmap之后，并没有将文件内容加载到物理页上，而是在虚拟内存中分配地址空间，当进程在访问这段地址时，通过查找页表，发现虚拟内存对应的页没有在物理内存中缓存则产生缺页中断，由内核的缺页异常处理程序处理，将文件对应内容以页为单位(4096)加载到物理内存中。\n" +
                "\n" +
                "由于物理内存是有限的，mmap在写入数据超过物理内存时，操作系统会进行页置换，根据淘汰算法，将需要淘汰的页置换成所需的新页，所以mmap对应的内存是可以被淘汰的，被淘汰的内存页如果是脏页（有过写操作修改页内容），则操作系统会先将数据回写磁盘再淘汰该页。\n" +
                "\n" +
                "数据写过程如下：\n" +
                "\n" +
                "1.将需要写入的数据写到对应的虚拟内存地址；\n" +
                "2.若对应的虚拟内存地址未对应物理内存，则产生缺页中断，由内核加载页数据到物理内存；\n" +
                "3.数据被写入到虚拟内存对应的物理内存；\n" +
                "4.在发生页淘汰或刷盘时由操作系统将脏页回写到磁盘。\n" +
                "RocketMQ正是利用MappedByteBuffer实现索引文件的读写，实现一个基于文件系统的HashMap。\n" +
                "\n" +
                "RocketMQ在创建新的CommitLog文件并通过FileChannel获取MappedByteBuffer时会做一次预热操作，即每个虚拟内存页（Page Cache）都写入四个字节的0x00，并强制刷盘将数据写到文件中。这个动作的用处是通过读写操作把MMAP映射全部加载到物理内存中。并且在预热之后还做了一个锁住内存的操作，这是为了避免磁盘交换，防止操作系统把预热过的页临时保存到swap区，防止程序再次读取交换出去的数据页时产生缺页中断。\n" +
                "\n" +
                "参考文献\n" +
                "【深入浅出Linux】关于mmap的解析\n" +
                "\n" +
                "分类：\n" +
                "阅读\n" +
                "标签：\n" +
                "Java\n" +
                "评论\n" +
                "\n" +
                "相关推荐\n" +
                "程序员乔戈里\n" +
                "2年前\n" +
                "Java\n" +
                "美团面试官问我一个字符的String.length()是多少，我说是1，面试官说你回去好好学一下吧\n" +
                "12.8w\n" +
                "470\n" +
                "160\n" +
                "艾小仙\n" +
                "17天前\n" +
                "Java\n" +
                "后端\n" +
                "前端\n" +
                "程序员最容易读错的单词，听到status我炸了\n" +
                "4.2w\n" +
                "195\n" +
                "295\n" +
                "Java技术栈\n" +
                "10月前\n" +
                "程序员\n" +
                "雷军做程序员时写的博客，太牛了。。\n" +
                "22.7w\n" +
                "776\n" +
                "218\n" +
                "涡流\n" +
                "11月前\n" +
                "面试\n" +
                "前端两年经验，历时一个月的面经和总结\n" +
                "8.6w\n" +
                "2419\n" +
                "199\n" +
                "摸鱼的春哥\n" +
                "7月前\n" +
                "前端\n" +
                "JavaScript\n" +
                "2022，前端的天\uD83C\uDF26️要怎么变？\n" +
                "15.5w\n" +
                "1508\n" +
                "658\n" +
                "楼下小黑哥\n" +
                "2年前\n" +
                "Java\n" +
                "求求你了，不要再自己实现这些逻辑了，开源工具类不香吗？\n" +
                "3.9w\n" +
                "528\n" +
                "72\n" +
                "MacroZheng\n" +
                "3月前\n" +
                "Java\n" +
                "后端\n" +
                "IntelliJ IDEA\n" +
                "好用到爆！IDEA版Postman面世了，功能真心强大！\n" +
                "8.7w\n" +
                "560\n" +
                "97\n" +
                "ConardLi\n" +
                "3年前\n" +
                "JavaScript\n" +
                "一名【合格】前端工程师的自检清单\n" +
                "26.9w\n" +
                "6777\n" +
                "615\n" +
                "非优秀程序员\n" +
                "9月前\n" +
                "前端\n" +
                "JavaScript\n" +
                "如何用 CSS 中写出超级美丽的阴影效果\n" +
                "80.1w\n" +
                "563\n" +
                "37\n" +
                "Rust\n" +
                "9月前\n" +
                "面试\n" +
                "程序员全职接单一个月的感触\n" +
                "16.0w\n" +
                "999\n" +
                "625\n" +
                "暮色妖娆丶\n" +
                "6月前\n" +
                "后端\n" +
                "Java\n" +
                "优秀的后端应该有哪些开发习惯？\n" +
                "7.2w\n" +
                "953\n" +
                "303\n" +
                "why技术\n" +
                "3月前\n" +
                "后端\n" +
                "Java\n" +
                "面试\n" +
                "这个队列的思路是真的好，现在它是我简历上的亮点了。\n" +
                "6.0w\n" +
                "625\n" +
                "69\n" +
                "阳光是sunny\n" +
                "5月前\n" +
                "前端\n" +
                "JavaScript\n" +
                "三面面试官：运行 npm run xxx 的时候发生了什么？\n" +
                "12.8w\n" +
                "4120\n" +
                "339\n" +
                "MacroZheng\n" +
                "4月前\n" +
                "后端\n" +
                "Java\n" +
                "Spring Boot\n" +
                "解放双手！推荐一款阿里开源的低代码工具，YYDS！\n" +
                "9.2w\n" +
                "903\n" +
                "134\n" +
                "愚公要移山\n" +
                "3年前\n" +
                "Java\n" +
                "这篇java的NIO编程，保证你能看懂\n" +
                "185\n" +
                "3\n" +
                "评论\n" +
                "程序员依扬\n" +
                "3年前\n" +
                "面试\n" +
                "前端\n" +
                "【1 月最新】前端 100 问：能搞懂 80% 的请把简历给我\n" +
                "57.5w\n" +
                "10001\n" +
                "343\n" +
                "柠檬鲸\n" +
                "2年前\n" +
                "Java\n" +
                "零基础小白入行,如何写出简洁干练的Java代码?\n" +
                "68\n" +
                "点赞\n" +
                "评论\n" +
                "程序猿DD\n" +
                "8月前\n" +
                "后端\n" +
                "Java\n" +
                "今年你因为 YYYY-MM-dd 被锤了吗？\n" +
                "6.5w\n" +
                "309\n" +
                "54\n" +
                "喜洋洋\n" +
                "1年前\n" +
                "Java\n" +
                "复制文件（最正确的写法） java\n" +
                "216\n" +
                "点赞\n" +
                "评论\n" +
                "gavin_wangzg\n" +
                "1年前\n" +
                "Java\n" +
                "像这样写，Java菜鸟也能写出牛逼的代码\n" +
                "92\n" +
                "点赞\n" +
                "评论\n" +
                "\n" +
                "Java艺术\n" +
                "lv-4\n" +
                "中间件研发 @ 荔枝集团\n" +
                "获得点赞 203\n" +
                "文章被阅读 149,470\n" +
                "\n" +
                "下载稀土掘金APP\n" +
                "一个帮助开发者成长的社区\n" +
                "\n" +
                "相关文章\n" +
                "ElasticSearch高版本API的使用姿势\n" +
                "1点赞  ·  1评论\n" +
                "vue中Axios的封装和API接口的管理\n" +
                "4515点赞  ·  333评论\n" +
                "使用Redis实现积分排行榜，并支持同积分按时间排序\n" +
                "9点赞  ·  3评论\n" +
                "延迟消息队列设计\n" +
                "10点赞  ·  1评论\n" +
                "一行代码完成 JAVA 的 EXCEL 读写——EasyExcel 的方法封装\n" +
                "413点赞  ·  26评论\n" +
                "目录\n" +
                "面向文件编程的重要性\n" +
                "简单文件读写\n" +
                "FileOutputStream\n" +
                "FileInputStream\n" +
                "随机访问文件读写\n" +
                "NIO文件读写-FileChannel\n" +
                "使用MappedByteBuffer读写文件\n" +
                "参考文献\n";
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public int getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(int goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
