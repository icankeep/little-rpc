package com.passer.littlerpc.core.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author passer
 * @time 2021/3/27 8:50 下午
 */
@Slf4j
public class ChannelCache {
    private final Map<String, Channel> cacheChannels = new ConcurrentHashMap<>();

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        if (cacheChannels.containsKey(key)) {
            Channel channel = cacheChannels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            removeChannel(inetSocketAddress);
        }
        return null;
    }

    public void addChannel(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        cacheChannels.put(key, channel);
        log.info("Add channel[{}] to cache.", key);
    }

    public void removeChannel(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        cacheChannels.remove(key);
        log.info("Remove channel[{}] in cache.", key);
    }
}
