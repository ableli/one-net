package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.session.ClientSession;
import com.weyong.onenet.dto.DataTransfer;
import com.weyong.zip.ByteZipUtil;
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
@EqualsAndHashCode(callSuper=false)
public class LocalInboudHandler extends ChannelInboundHandlerAdapter {
    private ClientSession clientSession;

    public LocalInboudHandler(ClientSession clientSession){
        this.clientSession = clientSession;
    }

    //Max frame size is 1048576 leave 1k byte to class info.
    private static int frameSize  = 1047576;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            while (in.readableBytes() > 0) {
                int length = frameSize < in.readableBytes() ? frameSize : in.readableBytes();
                byte[] currentData = new byte[length];
                in.readBytes(currentData, 0, length);
                DataTransfer dt = new DataTransfer();
                dt.setContextName(clientSession.getContextName());
                dt.setOpType(DataTransfer.OP_TYPE_DATA);
               // byte[] encryptData = WXBizMsgCrypt.getEncryptBytes(ByteZipUtil.gzip(currentData));
                byte[] encryptData =ByteZipUtil.gzip(currentData);
                dt.setData(encryptData);
                dt.setSessionId(clientSession.getSessionId());
                clientSession.getOneNetChannel().writeAndFlush(dt);
                log.info("Local data to OneNet.");
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
