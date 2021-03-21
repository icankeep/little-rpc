package com.passer.littlerpc.common.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.passer.littlerpc.common.exception.SerializeException;
import com.passer.littlerpc.common.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serializer {

    /**
     * kryo is not thread safe
     */
    ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = kryoThreadLocal.get();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)){
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Serialize failed: " + e.getMessage());
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             Input input = new Input(bis)) {
            Kryo kryo = kryoThreadLocal.get();
            T obj = kryo.readObjectOrNull(input, clazz);
            kryoThreadLocal.remove();
            return obj;
        } catch (Exception e) {
            throw new SerializeException("Deserialize failed: " + e.getMessage());
        }
    }
}
