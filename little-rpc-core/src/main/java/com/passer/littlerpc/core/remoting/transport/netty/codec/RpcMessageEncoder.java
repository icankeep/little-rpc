package com.passer.littlerpc.core.remoting.transport.netty.codec;

import com.passer.littlerpc.common.constants.MessageTypeEnum;
import com.passer.littlerpc.common.constants.RpcConstants;
import com.passer.littlerpc.common.constants.SerializerTypeEnum;
import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.common.remoting.dto.RpcMessage;
import com.passer.littlerpc.common.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author passer
 * @time 2021/3/26 11:13 下午
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        try {
            out.writeBytes(RpcConstants.MAGIC_STRING.getBytes(StandardCharsets.UTF_8));
            out.writeByte(RpcConstants.VERSION);
            // leave a place to write the value of message length
            out.writerIndex(out.writerIndex() + 4);
            out.writeByte(msg.getMessageType());
            out.writeByte(msg.getCodec());
            out.writeByte(msg.getCompress());
            out.writeInt(REQUEST_ID.getAndIncrement());
            int msgLength = RpcConstants.MESSAGE_HEADER_LENGTH;
            if (msg.getMessageType() != MessageTypeEnum.HEARTBEAT_REQUEST.ordinal()
                    && msg.getMessageType() != MessageTypeEnum.HEARTBEAT_RESPONSE.ordinal()) {
                String codec = SerializerTypeEnum.getName(msg.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codec);
                Object data = msg.getData();
                byte[] buffer = serializer.serialize(data);
                out.writeBytes(buffer);
                msgLength += buffer.length;
            }
            int endIndex = out.writerIndex();
            // go to full length index
            int index = endIndex - msgLength + RpcConstants.MAGIC_LENGTH + 1;
            out.writerIndex(index);
            out.writeInt(msgLength);
            out.writerIndex(endIndex);
        } catch (Exception e) {
            log.error("Message encode error.", e);
        }
    }
}
