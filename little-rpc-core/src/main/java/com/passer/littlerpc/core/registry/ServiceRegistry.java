package com.passer.littlerpc.core.registry;

import com.passer.littlerpc.common.annotation.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {
    /**
     * register service
     * @param rpcServiceName    rpc service name
     * @param address           service network address
     */
    void registerService(String rpcServiceName, InetSocketAddress address);
}
