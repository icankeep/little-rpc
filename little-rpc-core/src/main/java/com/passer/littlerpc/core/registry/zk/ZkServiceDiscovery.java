package com.passer.littlerpc.core.registry.zk;

import com.passer.littlerpc.core.registry.ServiceDiscovery;

import java.net.InetSocketAddress;

public class ZkServiceDiscovery implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        return null;
    }
}
