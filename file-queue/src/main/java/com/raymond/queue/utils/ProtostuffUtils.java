package com.raymond.queue.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff序列化工具
 *
 * @author :  raymond
 * @version :  V1.0
 * @date :  2021-01-21 10:27
 */
public class ProtostuffUtils {

    /**
     * 缓存Schema
     */
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    /**
     * 序列化方法，把指定对象序列化成字节数组
     *
     * @param obj 需要序列化的对象
     * @param <T> 泛型
     * @return 序列化后的字节
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializer(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Schema<T> schema = getSchema(clazz);

        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }

        return data;
    }

    /**
     * 序列化方法，把指定对象序列化成字节数组
     *
     * @param obj 需要序列化的对象
     * @param buffer 序列化过程中开辟的buffer
     * @param <T> 泛型
     * @return 序列化后的字节
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializer(T obj, LinkedBuffer buffer) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Schema<T> schema = getSchema(clazz);
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }

    }

    /**
     * 反序列化方法，将字节数组反序列化成指定Class类型
     *
     * @param data 需要反序列化的字节
     * @param clazz 需要转换的类型
     * @param <T> 泛型
     * @return 转换后的对象
     */
    public static <T> T deserializer(byte[] data, Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            //这个schema通过RuntimeSchema进行懒创建并缓存
            //所以可以一直调用RuntimeSchema.getSchema(),这个方法是线程安全的
            schema = RuntimeSchema.getSchema(clazz);
            if (Objects.nonNull(schema)) {
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }
}
