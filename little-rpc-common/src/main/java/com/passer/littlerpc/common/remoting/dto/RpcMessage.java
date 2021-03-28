package com.passer.littlerpc.common.remoting.dto;

import lombok.*;

/**
 * @author passer
 * @time 2021/3/27 11:05 上午
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    private byte messageType;
    private byte codec;
    private byte compress;
    private int requestId;
    private Object data;
}
