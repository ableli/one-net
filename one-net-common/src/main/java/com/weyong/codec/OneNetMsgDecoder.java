package com.weyong.codec;

import com.weyong.onenet.dto.BasePackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by haoli on 2018/4/7.
 */
public class OneNetMsgDecoder extends LengthFieldBasedFrameDecoder {
    public OneNetMsgDecoder() {
        super(1048576, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        BasePackage basePackage = BasePackage.fromBytes(frame);
        in.release();
        return basePackage;
    }
}
