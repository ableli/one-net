package com.weyong.codec;

import com.weyong.onenet.dto.BasePackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * It is a Netty channel Indound handler, It decoding a incoming frame to OneNet {@link BasePackage}, The encoding
 * process is in the {@link OneNetMsgEncoder}
 *
 * Created by haoli on 2018/4/7.
 */
public class OneNetMsgDecoder extends LengthFieldBasedFrameDecoder {

    /*
    * The max frame length is 1M, and the 4 byte on the front is the length of the frame.
     */
    public OneNetMsgDecoder() {
        super(1048576, 0, 4, 0, 4);
    }

    /**
    *  it is the method of Netty inbound handler, decode each frame to a BasePackage instance.
    *
     * @param ctx
     * @param in the receiving data in buf.
     * @return  the instance of a BasePackage
     * @see ChannelInboundHandlerAdapter
    */

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        BasePackage basePackage = BasePackage.fromBytes(frame);
        frame.release();
        return basePackage;
    }
}
