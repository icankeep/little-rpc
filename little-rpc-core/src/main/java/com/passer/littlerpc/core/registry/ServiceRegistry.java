package com.passer.littlerpc.core.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    /**
     * register service
     * @param rpcServiceName    rpc service name
     * @param address           service network address
     */
    void registerService(String rpcServiceName, InetSocketAddress address);
}
