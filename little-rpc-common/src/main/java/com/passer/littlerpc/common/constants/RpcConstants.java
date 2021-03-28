package com.passer.littlerpc.common.constants;

/**
 * @author passer
 * @time 2021/3/27 9:44 上午
 */
public interface RpcConstants {
    // 8MB
    int MAX_MESSAGE_FRAME_LENGTH = 8 * 1024 * 1024;
    int MESSAGE_HEADER_LENGTH = 16;
    int MAGIC_LENGTH = 4;
    byte VERSION = 1;
    String MAGIC_STRING = "LRPC";
    String HEARTBEAT_REQUEST_PING = "PING";
    String HEARTBEAT_RESPONSE_PONG = "PONG";

}
