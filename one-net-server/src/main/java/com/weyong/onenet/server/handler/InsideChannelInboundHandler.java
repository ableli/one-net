package com.weyong.onenet.server.handler;

import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.context.session.OneNetSession;
import com.weyong.zip.ByteZipUtil;
import com.weyong.aes.WXBizMsgCrypt;
import com.weyong.onenet.dto.DataTransfer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Author by haoli on 2017/4/12.
 */
@Slf4j
public class InsideChannelInboundHandler extends SimpleChannelInboundHandler<DataTransfer> {
    private OneNetServerContext oneNetServerContext;
    private OneNetServer oneNetServer;
    public static final ConcurrentHashMap<String, Channel> outsideChannelMap
            = new ConcurrentHashMap<String, Channel>();

    public InsideChannelInboundHandler(OneNetServer oneNetServer){
        this.oneNetServer = oneNetServer;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("exception in one channel inbound:" + cause.getMessage());
        ctx.channel().close();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataTransfer msg) throws Exception {
        switch (msg.getOpType()){
            case DataTransfer.OP_TYPE_HEART_BEAT:
                if (oneNetServerContext != null) {
                    msg.setAes(oneNetServerContext.getOneNetServerContextConfig().isAes());
                    msg.setZip(oneNetServerContext.getOneNetServerContextConfig().isZip());
                }
                ctx.channel().writeAndFlush(msg);
                break;
            case DataTransfer.OP_TYPE_NEW:
                oneNetServer.getContexts().compute(msg.getOneNetName(),(key,oneNetServer)->{
                    if(oneNetServer==null){
                        ctx.channel().writeAndFlush(new DataTransfer(DataTransfer.OP_TYPE_ERROR,String.format("OneNet %s's config not found in Server", key).getBytes()));
                    }else{
                        Channel oldChannel = this.oneNetServer.getOneNetConnectionManager().getOneNetChannels().replace(key, ctx.channel());
                        if(oldChannel!=null){
                            oldChannel.close();
                        }
                    }
                    return oneNetServer;
                });
                break;
            case DataTransfer.OP_TYPE_CLOSE:
                if(oneNetServerContext!=null){
                    oneNetServerContext.closeSession(msg.getSessionId());
                }
                break;
            default:
                if(oneNetServerContext==null){
                    return;
                }
                OneNetSession oneNetSession =
                    oneNetServerContext.getOneNetSessions().get(msg.getSessionId());
                if (oneNetSession != null) {
                    byte[] outputData=msg.getData();
                    if(oneNetServerContext.getOneNetServerContextConfig().isAes()){
                        outputData = WXBizMsgCrypt.getDecryptBytes(outputData);
                    }
                    if(oneNetServerContext.getOneNetServerContextConfig().isZip()){
                        outputData = ByteZipUtil.unGzip(outputData);
                    }
                    oneNetSession.getInternetChannel().writeAndFlush(outputData);
                } else {
                    log.info("Can't find exist session :" + msg.getSessionId());
                }
        }
    }
}
