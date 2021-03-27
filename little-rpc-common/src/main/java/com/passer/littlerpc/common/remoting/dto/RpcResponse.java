package com.passer.littlerpc.common.remoting.dto;

import com.passer.littlerpc.common.constants.RpcResponseEnum;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = -6763018384475687293L;
    private int requestId;
    private T data;
    private Integer code;
    private String message;

    public static <T> RpcResponse<T> success(T data, int requestId) {
        RpcResponse<T> rpcResponse = new RpcResponseBuilder<T>()
                .code(RpcResponseEnum.SUCCESS.getCode())
                .data(data)
                .message(RpcResponseEnum.SUCCESS.getMessage())
                .requestId(requestId)
                .build();
        return rpcResponse;
    }

    public static <T> RpcResponse<T> fail(T data, int requestId) {
        RpcResponse<T> rpcResponse = new RpcResponseBuilder<T>()
                .code(RpcResponseEnum.FAIL.getCode())
                .message(RpcResponseEnum.FAIL.getMessage())
                .data(data)
                .requestId(requestId)
                .build();
        return rpcResponse;
    }
}
