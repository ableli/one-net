package com.weyong.onenet.server.handler;

import com.weyong.onenet.dto.DataTransfer;
import com.weyong.onenet.server.session.OneNetSession;
import com.weyong.zip.ByteZipUtil;
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
            while (in.readableBytes() > 0) {
                int length = frameSize < in.readableBytes() ? frameSize : in.readableBytes();
                byte[] currentData = new byte[length];
                in.readBytes(currentData, 0, length);
                DataTransfer dt = new DataTransfer();
                dt.setContextName(oneNetSession.getContextName());
                dt.setOpType(DataTransfer.OP_TYPE_DATA);
                dt.setData(ByteZipUtil.gzip(currentData));
                dt.setSessionId(oneNetSession.getSessionId());
                log.info("Internet data send to OneNet.");
                oneNetSession.getOneNetChannel().writeAndFlush(dt);

            }
            in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        oneNetSession.closeFromClient();
        ctx.close();
        log.info(cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        oneNetSession.closeFromClient();
        super.channelInactive(ctx);
    }

}
