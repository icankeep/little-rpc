package com.passer.littlerpc.core.remoting.transport;

import com.passer.littlerpc.common.remoting.dto.RpcRequest;

/**
 * @author passer
 * @time 2021/3/27 8:26 下午
 */
public interface RpcRequestTransport {
    /**
     * send rpc request to server and return result
     * @param request
     * @return
     */
    Object sendRpcRequest(RpcRequest request);
}
