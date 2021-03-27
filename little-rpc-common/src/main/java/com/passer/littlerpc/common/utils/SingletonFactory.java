package com.passer.littlerpc.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {

    private SingletonFactory() {}

    private static final Map<Class<?>, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    public static <T> T getInstance(Class<T> clazz) {
        Object obj = OBJECT_MAP.get(clazz);
        if (obj == null) {
            try {
                obj = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            OBJECT_MAP.putIfAbsent(clazz, obj);
            obj = OBJECT_MAP.get(clazz);
        }
        return clazz.cast(obj);
    }

/*    // 普通实现单例方法
    private static final Map<Class<?>, Object> OBJECT_MAP = new HashMap<>();

    public static <T> T getInstance(Class<T> clazz) {
        Object obj = OBJECT_MAP.get(clazz);
        if (obj != null) {
            return clazz.cast(obj);
        }

        synchronized (SingletonFactory.class) {
            obj = OBJECT_MAP.get(clazz);
            if (obj == null) {
                try {
                    obj = clazz.getDeclaredConstructor().newInstance();
                    OBJECT_MAP.put(clazz, obj);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return clazz.cast(obj);
    }*/
}
