package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.context.OneNetClientContext;
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
    private ClientSession clientSession;
    private OneNetClientContext oneNetClientContext;

    public LocalInboudHandler(OneNetClientContext oneNetClientContext, ClientSession clientSession) {
        this.clientSession = clientSession;
        this.oneNetClientContext = oneNetClientContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        while (in.readableBytes() > 0) {
            int length = DataPackage.FRAME_MAX_SIZE < in.readableBytes() ? DataPackage.FRAME_MAX_SIZE : in.readableBytes();
            byte[] currentData = new byte[length];
            in.readBytes(currentData, 0, length);
            DataPackage dt = new DataPackage(clientSession.getContextName(),
                    clientSession.getSessionId(),
                    currentData,
                    getOneNetClientContext().isZip(),
                    getOneNetClientContext().isAes());
            clientSession.getServerSession().getServerChannel().writeAndFlush(dt);
        }
        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.oneNetClientContext.close(clientSession);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.oneNetClientContext.close(clientSession);
        super.channelInactive(ctx);
    }
}
