package com.weyong.onenet.server.handler;

import com.weyong.onenet.dto.DataPackage;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.session.OneNetSession;
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
@EqualsAndHashCode(callSuper = false)
public class InternetChannelInboundHandler extends ChannelInboundHandlerAdapter {
    //Max frame size is 1048576 leave 1k byte to class info.
    private static int frameSize = 1047576;

    private OneNetSession oneNetSession;
    private OneNetServerContext oneNetServerContext;

    public InternetChannelInboundHandler(OneNetServerContext oneNetServerContext,OneNetSession oneNetSession) {
        this.oneNetServerContext = oneNetServerContext;
        this.oneNetSession = oneNetSession;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        while (in.readableBytes() > 0) {
            int length = frameSize < in.readableBytes() ? frameSize : in.readableBytes();
            byte[] currentData = new byte[length];
            in.readBytes(currentData, 0, length);
            DataPackage dt = new DataPackage(
                    oneNetServerContext.getOneNetServerContextConfig().getContextName(),
                    oneNetSession.getSessionId(),
                    currentData,
                    oneNetServerContext.getOneNetServerContextConfig().isZip(),
                    oneNetServerContext.getOneNetServerContextConfig().isAes());
            oneNetSession.getOneNetChannel().writeAndFlush(dt);

        }
        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        this.oneNetServerContext.close(oneNetSession);
        log.error(cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.oneNetServerContext.close(oneNetSession);
        super.channelInactive(ctx);
    }

}
