package com.passer.littlerpc.common.exception;

import com.passer.littlerpc.common.constants.RpcErrorMessageEnum;

public class RpcException extends RuntimeException {
    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum errorMessageEnum, String detail) {
        super(String.format(errorMessageEnum.getMessage() + "[" + detail + "]"));
    }
}
