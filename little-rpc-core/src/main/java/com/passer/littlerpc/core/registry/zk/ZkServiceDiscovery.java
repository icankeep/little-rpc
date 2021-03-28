package com.passer.littlerpc.core.registry.zk;

import com.passer.littlerpc.common.constants.RpcErrorMessageEnum;
import com.passer.littlerpc.common.exception.RpcException;
import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.core.loadbalance.LoadBalance;
import com.passer.littlerpc.core.registry.ServiceDiscovery;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscovery implements ServiceDiscovery {

    private final LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("random");

    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        String path = CuratorUtils.getZkRootPath() + rpcServiceName;
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(path);
        String address = loadBalance.selectService(childrenNodes);
        if (StringUtils.isBlank(address)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND, rpcServiceName);
        }
        return convertStringToInetSocketAddress(address);
    }

    private InetSocketAddress convertStringToInetSocketAddress(String address) {
        String[] hostAndPort = address.split(":");
        if (hostAndPort.length != 2) {
            throw new RpcException("address format error:" + address);
        }
        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);
        return new InetSocketAddress(host, port);
    }
}
