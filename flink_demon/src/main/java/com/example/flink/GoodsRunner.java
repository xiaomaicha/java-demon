package com.example.flink;/*
 * @Classname GoodsRunner
 * @Description
 * @Date 2022/9/8 0:11
 * @author by dell
 */

import com.alibaba.fastjson.JSON;
import com.example.filequeue.queue.PermanentQueue;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class GoodsRunner implements Runnable {

    private final LinkedBlockingQueue<Goods> linkedBlockingDeque;

    public GoodsRunner(LinkedBlockingQueue<Goods> linkedBlockingDeque) {
        this.linkedBlockingDeque = linkedBlockingDeque;
    }

    @Override
    public void run() {

        while (true) {
            try {
//                String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
//                String processID = processName.substring(0,processName.indexOf('@'));
//                System.out.println("processID="+processID);

                Thread.sleep(0, 5);
                Goods goods = new Goods();
                goods.setGoodsId("1");
                goods.setDate(new Date());

                PermanentQueue.offer("goods", JSON.toJSONString(goods));
//                linkedBlockingDeque.put(goods);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
