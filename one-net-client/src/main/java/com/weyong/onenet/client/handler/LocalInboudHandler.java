package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.clientSession.ClientSession;
import com.weyong.zip.ByteZipUtil;
import com.weyong.aes.WXBizMsgCrypt;
import com.weyong.onenet.dto.DataTransfer;
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
        while(in.readableBytes()>0){
            int length = frameSize<in.readableBytes()?frameSize:in.readableBytes();
            byte[] currentData =new byte[length];
            in.readBytes(currentData, 0 ,length);
            DataTransfer dt = new DataTransfer();
            byte[] encryptData = WXBizMsgCrypt.getEncryptBytes(ByteZipUtil.gzip(currentData));
            dt.setData(encryptData);
            dt.setSessionId(clientSession.getSessionId());
            clientSession.getOneNetChannel().writeAndFlush(dt);
        }
        in.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        clientSession.close();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientSession.close();
        super.channelInactive(ctx);
    }
}
