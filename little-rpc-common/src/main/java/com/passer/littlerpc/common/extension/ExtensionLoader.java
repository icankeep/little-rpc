package com.passer.littlerpc.common.extension;

import com.passer.littlerpc.common.annotation.SPI;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader<T> {
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADER_MAP = new ConcurrentHashMap<>();
    private Class<?> type;

    public ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("ExtensionLoader type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("ExtensionLoader type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("ExtensionLoader type must be annotated by @SPI.");
        }
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADER_MAP.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADER_MAP.put(type, new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADER_MAP.get(type);
        }
        return extensionLoader;
    }
}
