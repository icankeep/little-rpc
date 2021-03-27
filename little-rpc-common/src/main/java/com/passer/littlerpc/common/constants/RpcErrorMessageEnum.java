package com.passer.littlerpc.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author passer
 * @time 2021/3/27 7:30 下午
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessageEnum {
    CLIENT_CONNECTION_ERROR("客户端连接服务端失败"),
    SERVICE_INVOCATION_ERROR("服务调用失败"),
    SERVICE_NOT_FOUND("没有找到指定的服务"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现任何接口"),
    REQUEST_NOT_MATCH_RESPONSE("返回结果错误, 请求和返回的相应不匹配");
    private final String message;
}
