package com.passer.littlerpc.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseEnum {
    SUCCESS(200, "The remote call is success!"),
    FAIL(500, "The remote call is fail!");
    private int code;
    private String message;
}
