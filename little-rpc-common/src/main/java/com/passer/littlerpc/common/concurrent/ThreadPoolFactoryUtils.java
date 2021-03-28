package com.passer.littlerpc.common.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolFactoryUtils {
    private ThreadPoolFactoryUtils() {}

    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (StringUtils.isNotBlank(threadNamePrefix)) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon)
                        .build();
            }
            return new ThreadFactoryBuilder()
                    .setNameFormat(threadNamePrefix + "-%d")
                    .build();
        }
        return Executors.defaultThreadFactory();
    }
}
