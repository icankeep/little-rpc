package com.passer.littlerpc.common.serialize.protostuff;

import com.passer.littlerpc.common.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializer implements Serializer {
    /**
     * Re-use (manage) this buffer to avoid allocating on every serialization
     */
    private static final LinkedBuffer LINKED_BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public byte[] serialize(Object obj) {
        Class<?> clz = obj.getClass();
        Schema schema = RuntimeSchema.getSchema(clz);
        final byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, LINKED_BUFFER);
        } finally {
            LINKED_BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
