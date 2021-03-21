package com.passer.littlerpc.common.serialize;

public interface Serializer {
    /**
     * 将对象序列化成byte数组
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);

    /**
     * 将byte数组转为对应类型的对象
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
