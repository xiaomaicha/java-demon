package com.raymond.queue.callback;


/**
 * 消费业务service
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-07-25 11:07
 */
public interface ConsumeService<T> {
    /**
     * 业务处理
     * @param data 消费的数据
     * @return true为确认消费
     */
    boolean doService(T data);

    /**
     * 无数据处理逻辑
     */
    default void nullService() {

    }

}
