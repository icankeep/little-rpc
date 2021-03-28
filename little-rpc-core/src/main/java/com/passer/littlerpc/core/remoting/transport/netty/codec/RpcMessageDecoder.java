package com.passer.littlerpc.core.remoting.transport.netty.codec;

import com.passer.littlerpc.common.constants.MessageTypeEnum;
import com.passer.littlerpc.common.constants.RpcConstants;
import com.passer.littlerpc.common.constants.SerializerTypeEnum;
import com.passer.littlerpc.common.exception.ValidMessageException;
import com.passer.littlerpc.common.remoting.dto.RpcMessage;
import com.passer.littlerpc.common.remoting.dto.RpcRequest;
import com.passer.littlerpc.common.remoting.dto.RpcResponse;
import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.common.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;


/**
 * custom protocol decoder
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 * </p>
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

/**
 * @author passer
 * @time 2021/3/26 11:13 下午
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_MESSAGE_FRAME_LENGTH, 5, 4, -9, 0);
    }


    public RpcMessageDecoder(int maxFrameLength,
                             int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf)decode;
            if (frame.readableBytes() >= RpcConstants.MESSAGE_HEADER_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("decode frame error", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf in) {
        // read data in order
        checkMagicNumber(in);
        checkVersion(in);
        int messageLength = in.readInt();
        byte messageType = in.readByte();
        byte codec = in.readByte();
        byte compress = in.readByte();
        int requestId = in.readInt();

        RpcMessage message = RpcMessage.builder()
                .messageType(messageType)
                .codec(codec)
                .compress(compress)
                .requestId(requestId)
                .build();

        if (messageType == MessageTypeEnum.HEARTBEAT_REQUEST.ordinal()) {
            message.setData(RpcConstants.HEARTBEAT_REQUEST_PING);
            return message;
        }
        if (messageType == MessageTypeEnum.HEARTBEAT_RESPONSE.ordinal()) {
            message.setData(RpcConstants.HEARTBEAT_RESPONSE_PONG);
            return message;
        }
        int bodyLength = messageLength - RpcConstants.MESSAGE_HEADER_LENGTH;
        if (bodyLength > 0) {
            byte[] buffer = new byte[bodyLength];
            in.readBytes(buffer);
            // get codec type for serialize
            String codecName = SerializerTypeEnum.getName(message.getCodec());
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if (messageType == MessageTypeEnum.NORMAL_REQUEST.ordinal()) {
                RpcRequest request = serializer.deserialize(buffer, RpcRequest.class);
                message.setData(request);
            }
            if (messageType == MessageTypeEnum.NORMAL_RESPONSE.ordinal()) {
                RpcResponse response = serializer.deserialize(buffer, RpcResponse.class);
                message.setData(response);
            }
        }
        return message;
    }

    private void checkMagicNumber(ByteBuf in) {
        byte[] magic = new byte[RpcConstants.MAGIC_LENGTH];
        in.readBytes(magic);
        String data = new String(magic, StandardCharsets.UTF_8);
        if (!RpcConstants.MAGIC_STRING.equals(data)) {
            throw new ValidMessageException(String.format("Valid magic code, except: %s, get %s",
                    RpcConstants.MAGIC_STRING, data));
        }
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException(String.format("Version isn't compatible, except: %s, get: %s",
                    RpcConstants.VERSION, version));
        }
    }
}
