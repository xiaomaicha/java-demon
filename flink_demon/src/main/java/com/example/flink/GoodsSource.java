package com.example.flink;/*
 * @Classname GoodsSource
 * @Description
 * @Date 2022/9/7 23:50
 * @author by dell
 */

import com.alibaba.fastjson.JSON;
import com.example.filequeue.queue.PermanentQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class GoodsSource implements SourceFunction<Goods> {

    private final LinkedBlockingQueue<Goods> linkedBlockingDeque;

    public GoodsSource(LinkedBlockingQueue<Goods> linkedBlockingDeque) {
        this.linkedBlockingDeque = linkedBlockingDeque;
    }

    @Override
    public void run(SourceContext<Goods> ctx) throws Exception {
        while (true) {
//            Thread.sleep(500);
//            Goods goods = new Goods();
//            goods.setGoodsId("1");
//            goods.setDate(new Date());

            String s = PermanentQueue.take("goods");
            Goods goods = JSON.parseObject(s, Goods.class);
            ctx.collect(goods);

//            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
//            String processID = processName.substring(0, processName.indexOf('@'));
//            System.out.println("processID=" + processID);
//            System.out.println(Thread.currentThread().getName() + linkedBlockingDeque);
//            Goods goods = linkedBlockingDeque.take();

////            System.out.println(Thread.currentThread().getName() + goods);
//            ctx.collect(goods);

        }

    }

    @Override
    public void cancel() {

    }
}
