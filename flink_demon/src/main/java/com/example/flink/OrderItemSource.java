package com.example.flink;/*
 * @Classname OrderItemSource
 * @Description
 * @Date 2022/9/7 23:53
 * @author by dell
 */

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Date;

public class OrderItemSource implements SourceFunction<OrderItem> {
    @Override
    public void run(SourceContext<OrderItem> ctx) throws Exception {
        while (true) {
            OrderItem orderItem = new OrderItem();
            orderItem.setGoodsId("1");
            orderItem.setDate(new Date());
            ctx.collect(orderItem);
            Thread.sleep(500);
        }
    }

    @Override
    public void cancel() {

    }
}
