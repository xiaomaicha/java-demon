package com.example.flink;/*
 * @Classname IntervalJoin
 * @Description
 * @Date 2022/9/7 23:29
 * @author by dell
 */


import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @projectname: HaiStream
 * @description:
 * @author: Mr.Zhang
 * @create: 2021-03-14 14:35
 **/
@Component
public class IntervalJoin {

    @PostConstruct
    public void run() throws Exception {

        LinkedBlockingQueue<Goods> goodsLinkedBlockingQueue = new LinkedBlockingQueue<>(100);

        new Thread(new GoodsRunner(goodsLinkedBlockingQueue)).start();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // 构建商品数据流
        SingleOutputStreamOperator<Goods> goodsDS = env.addSource(new GoodsSource(goodsLinkedBlockingQueue), TypeInformation.of(Goods.class))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Goods>() {
                    @Override
                    public long extractAscendingTimestamp(Goods element) {
                        return element.getDate().getTime();
                    }
                });
        // 构建订单明细数据流
        SingleOutputStreamOperator<OrderItem> orderItemDS = env.addSource(new OrderItemSource(), TypeInformation.of(OrderItem.class))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OrderItem>() {
                    @Override
                    public long extractAscendingTimestamp(OrderItem element) {
                        return element.getDate().getTime();
                    }
                });

        // 进行关联查询
        //todo 1、这里我们通过keyBy将两个流join到一起
        SingleOutputStreamOperator<FactOrderItem> factOrderItemDS = orderItemDS.keyBy(item -> item.getGoodsId())
                //todo 2、interval join需要设置流A去关联哪个时间范围的流B中的元素。
                .intervalJoin(goodsDS.keyBy(goods -> goods.getGoodsId()))
                //todo 此处，我设置的下界为-1、上界为0，
                .between(Time.seconds(-1), Time.seconds(1))
                //todo  且上界是一个开区间。表达的意思就是流A中某个元素的时间，对应上一秒的流B中的元素。
                .upperBoundExclusive()
                //todo process中将两个key一样的元素，关联在一起，并加载到一个新的FactOrderItem对象中
                .process(new ProcessJoinFunction<OrderItem, Goods, FactOrderItem>() {
                    @Override
                    public void processElement(OrderItem left, Goods right, Context ctx, Collector<FactOrderItem> out) throws Exception {
                        FactOrderItem factOrderItem = new FactOrderItem();
                        factOrderItem.setGoodsId(right.getGoodsId());
                        factOrderItem.setGoodsName(right.getGoodsName());
                        factOrderItem.setCount(left.getCount());
                        factOrderItem.setTotalMoney(right.getGoodsPrice() * left.getCount());

                        out.collect(factOrderItem);
                    }
                });

//        factOrderItemDS.print();

        env.execute("Interval JOIN");
    }
}