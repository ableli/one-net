package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.session.ClientSession;
import com.weyong.onenet.dto.DataPackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class LocalInboudHandler extends ChannelInboundHandlerAdapter {
    //Max frame size is 1048576 leave 1k byte to class info.
    private static int frameSize = 1047576;
    private ClientSession clientSession;

    public LocalInboudHandler(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        while (in.readableBytes() > 0) {
            int length = frameSize < in.readableBytes() ? frameSize : in.readableBytes();
            byte[] currentData = new byte[length];
            in.readBytes(currentData, 0, length);
            DataPackage dt = new DataPackage(clientSession.getContextName(),
                    clientSession.getSessionId(),
                    currentData,
                    clientSession.getOneNetClientContext().isZip(),
                    clientSession.getOneNetClientContext().isAes());
            clientSession.getServerSession().getServerChannel().writeAndFlush(dt);
        }
        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        clientSession.closeFromLocal();
        ctx.close();
        log.info(cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientSession.closeFromLocal();
        super.channelInactive(ctx);
    }
}
