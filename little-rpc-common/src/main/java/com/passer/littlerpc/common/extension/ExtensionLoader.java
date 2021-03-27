package com.passer.littlerpc.common.extension;

import com.passer.littlerpc.common.annotation.SPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtensionLoader<T> {
    private static final String EXTENSION_FILE_DIRECTORY = "META-INF/extensions/";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADER_MAP = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    private final Map<String, Holder<?>> cacheInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cacheClazz = new Holder<>();
    private Class<?> type;

    public ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
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

    public T getExtension(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be blank.");
        }
        Holder<T> holder = (Holder<T>)cacheInstances.get(name);
        if (holder == null) {
            cacheInstances.put(name, new Holder<T>());
            holder = (Holder<T>)cacheInstances.get(name);
        }
        T instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return instance;
    }

    private T createExtension(String name) {
        Map<String ,Class<?>> classMap = getExtensionClassMap(name);
        Class<?> clazz = classMap.get(name);
        if (clazz == null) {
            throw new RuntimeException(String.format("No such extension of name[%s]", name));
        }

        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.put(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClassMap(String name) {
        Map<String ,Class<?>> classMap = cacheClazz.get();
        if (classMap == null) {
            synchronized (cacheClazz) {
                classMap = cacheClazz.get();
                if (classMap == null) {
                    classMap = new HashMap<>();
                    loadExtension(classMap);
                    cacheClazz.set(classMap);
                }
            }
        }
        return classMap;
    }

    private void loadExtension(Map<String, Class<?>> classMap) {
        String path = EXTENSION_FILE_DIRECTORY + this.type.getName();
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        try {
            Enumeration<URL> urls = classLoader.getResources(path);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    loadExtensionResource(classMap, classLoader, urls.nextElement());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    private void loadExtensionResource(Map<String, Class<?>> classMap, ClassLoader classLoader, URL url) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci).trim();
                }
                if (line.length() > 0) {
                    int ei = line.indexOf('=');
                    String name = line.substring(0, ei).trim();
                    String className = line.substring(ei + 1).trim();
                    if (name.length() > 0 && className.length() > 0) {
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            classMap.put(name, clazz);
                        } catch (ClassNotFoundException e) {
                            log.error(String.format("Class[%s] not found", className), e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
