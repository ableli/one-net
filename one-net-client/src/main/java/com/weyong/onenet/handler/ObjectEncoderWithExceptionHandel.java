package com.weyong.onenet.handler;

import com.weyong.onenet.dto.DataTransfer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by haoli on 6/10/2017.
 */
@Slf4j
public class ObjectEncoderWithExceptionHandel extends ObjectEncoder {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        log.info("Exception when sending data to one channel:"+cause.getMessage());
    }
}
