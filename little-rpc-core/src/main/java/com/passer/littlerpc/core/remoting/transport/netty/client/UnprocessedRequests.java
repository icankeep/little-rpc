package com.passer.littlerpc.core.remoting.transport.netty.client;

import com.passer.littlerpc.common.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author passer
 * @time 2021/3/27 10:19 下午
 */
@Slf4j
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_FUTURE = new ConcurrentHashMap<>();

    public static void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_FUTURE.put(requestId, future);
    }

    public static void complete(RpcResponse<Object> response) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_FUTURE.get(response.getRequestId());
        if (future != null) {
            future.complete(response);
        } else {
            throw new IllegalStateException();
        }
    }
}
