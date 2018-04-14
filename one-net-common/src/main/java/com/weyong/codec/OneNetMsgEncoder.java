package com.weyong.codec;

import com.weyong.onenet.dto.BasePackage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

/**
 * Created by haoli on 2018/4/7.
 */
public class OneNetMsgEncoder extends MessageToByteEncoder<BasePackage> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    @Override
    protected void encode(ChannelHandlerContext ctx, BasePackage msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();
        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        if (msg.getByteBufferThreadLocal().get() == null) {
            msg.getByteBufferThreadLocal().set(ByteBuffer.allocate(1048576));
        }
        try {
            bout.write(LENGTH_PLACEHOLDER);
            bout.write(msg.toBytes());
            bout.flush();
        } finally {
            bout.close();
        }

        int endIdx = out.writerIndex();
        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
