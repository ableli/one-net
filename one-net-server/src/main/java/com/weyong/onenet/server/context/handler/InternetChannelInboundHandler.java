package com.weyong.onenet.server.context.handler;

import com.weyong.onenet.server.context.session.OneNetSession;
import com.weyong.zip.ByteZipUtil;
import com.weyong.onenet.dto.DataTransfer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hao.li on 2017/4/12.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper=false)
public class InternetChannelInboundHandler extends ChannelInboundHandlerAdapter {
    //Max frame size is 1048576 leave 1k byte to class info.
    private static int frameSize  = 1047576;

    private OneNetSession oneNetSession;

    public InternetChannelInboundHandler(OneNetSession oneNetSession) {
        this.oneNetSession = oneNetSession;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        while(in.readableBytes()>0) {
            int length = frameSize < in.readableBytes() ? frameSize : in.readableBytes();
            byte[] currentData = new byte[length];
            in.readBytes(currentData, 0, length);
            DataTransfer dt = new DataTransfer();
            dt.setData(ByteZipUtil.gzip(currentData));
            dt.setSessionId(oneNetSession.getSessionId());
            oneNetSession.getOneNetChannel().writeAndFlush(dt);

        }
        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        oneNetSession.close();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        oneNetSession.close();
        super.channelInactive(ctx);
    }

}
