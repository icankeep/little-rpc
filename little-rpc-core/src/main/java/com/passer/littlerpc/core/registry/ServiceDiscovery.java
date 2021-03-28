package com.passer.littlerpc.core.registry;

import com.passer.littlerpc.common.annotation.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {
    /**
     * look up service by service name
     * @param rpcServiceName    rpc service name
     * @return                  service's network address
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
