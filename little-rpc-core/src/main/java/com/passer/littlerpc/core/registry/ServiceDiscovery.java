package com.passer.littlerpc.core.registry;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    /**
     * look up service by service name
     * @param rpcServiceName    rpc service name
     * @return                  service's network address
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
