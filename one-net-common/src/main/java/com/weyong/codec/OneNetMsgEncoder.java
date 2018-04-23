package com.weyong.codec;

import com.weyong.onenet.dto.BasePackage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

/**
 * It is a Netty channel outbound handler, it encoding the Msg object to byte array
 * and apply the length frame protocol
 *
 * Each Netty worker thread has a reusable {@link ByteBuffer} in define at {@link BasePackage}
 * {@link ThreadLocal} byteBufferThreadLocal.
 *
 * Created by haoli on 2018/4/7.
 */
public class OneNetMsgEncoder extends MessageToByteEncoder<BasePackage> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    @Override
    protected void encode(ChannelHandlerContext ctx, BasePackage msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();
        out.writeBytes(LENGTH_PLACEHOLDER);
        out.writeBytes(msg.toBytes());
        int endIdx = out.writerIndex();
        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
