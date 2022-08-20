package com.raymond.queue.collection;


import com.raymond.queue.BlockingProduction;
import com.raymond.queue.utils.ProtostuffUtils;

import java.io.IOException;

/**
 * 集合对象的生产者
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2022-01-21 17:31
 */
public class CollectProduction<E> extends BlockingProduction<E> {

    public CollectProduction(String path, String topic, long fileSize) throws IOException {
        super(path, topic, fileSize);
    }

    @Override
    protected byte[] getBytes(E e) {
        return ProtostuffUtils.serializer(new CollectionEntry<>(e), super.buffer);
    }
}
