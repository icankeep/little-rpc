package com.passer.littlerpc.core.registry.zk;

import com.passer.littlerpc.core.registry.ServiceRegistry;
import org.apache.curator.utils.PathUtils;

import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress address) {
        String path = generateZkPath(rpcServiceName, address);
        CuratorUtils.createPersistentNode(path);
    }

    private String generateZkPath(String rpcServiceName, InetSocketAddress address) {
        return CuratorUtils.getZkRootPath() + rpcServiceName + address.toString();
    }
}
