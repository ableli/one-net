package com.weyong.onenet.server.handler;

import com.weyong.onenet.dto.DataPackage;
import com.weyong.onenet.server.session.OneNetHttpSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequestDecoder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hao.li on 2017/4/12.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class HttpRawDataHandler extends HttpRequestDecoder {

    private static int frameSize = 1047576;
    private OneNetHttpSession httpSession;

    public HttpRawDataHandler(OneNetHttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        while (in.readableBytes() > 0) {
            int length = frameSize < in.readableBytes() ? frameSize : in.readableBytes();
            byte[] currentData = new byte[length];
            in.readBytes(currentData, 0, length);
            DataPackage dt = new DataPackage(
                    null,
                    httpSession.getSessionId(),
                    currentData,
                    false,
                    false);
            httpSession.getQueue().add(dt);
        }
        in.resetReaderIndex();
        super.channelRead(ctx, msg);
    }

}
