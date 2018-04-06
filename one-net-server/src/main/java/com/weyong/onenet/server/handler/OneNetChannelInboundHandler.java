package com.weyong.onenet.server.handler;

import com.weyong.aes.WXBizMsgCrypt;
import com.weyong.onenet.dto.DataTransfer;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.session.OneNetSession;
import com.weyong.zip.ByteZipUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author by haoli on 2017/4/12.
 */
@Slf4j
public class OneNetChannelInboundHandler extends SimpleChannelInboundHandler<DataTransfer> {
    private OneNetServer oneNetServer;
    public static final ConcurrentHashMap<String, Channel> outsideChannelMap
            = new ConcurrentHashMap<String, Channel>();

    public OneNetChannelInboundHandler(OneNetServer oneNetServer){
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
        try {
            switch (msg.getOpType()) {
                case DataTransfer.OP_TYPE_HEART_BEAT:
                    ctx.channel().writeAndFlush(msg);
                    break;
                case DataTransfer.OP_TYPE_NEW:
                    if (CollectionUtils.isEmpty(msg.getContextNames())) {
                        ctx.close();
                        break;
                    }
                    log.info(String.format("Client %s with contexts %s request connections.",
                            msg.getClientName(), StringUtils.join(msg.getContextNames(), ",")));
                    List<String> toRemoveContextName = new ArrayList<>();
                    msg.getContextNames().stream().forEach((contextName) -> {
                        if (!oneNetServer.getContexts().containsKey(contextName)) {
                            ctx.channel().writeAndFlush(new DataTransfer(DataTransfer.OP_TYPE_ERROR,
                                    String.format("OneNet %s's config not found in Server", contextName).getBytes()));
                            toRemoveContextName.add(contextName);
                        }
                    });
                    msg.getContextNames().removeAll(toRemoveContextName);
                    if (CollectionUtils.containsAny(Collections.list(oneNetServer.getContexts().keys()), msg.getContextNames())) {
                        this.oneNetServer.getOneNetConnectionManager().refreshSessionChannel(msg.getClientName(), msg.getContextNames(), ctx.channel());
                    }
                    break;
                case DataTransfer.OP_TYPE_CLOSE:
                    OneNetServerContext oneNetServerContext = oneNetServer.getContexts().get(msg.getContextName());
                    if (oneNetServerContext != null) {
                        oneNetServerContext.getOneNetSessions().get(msg.getSessionId()).closeFromOneNet();
                    }
                    break;
                case DataTransfer.OP_TYPE_DATA:
                    oneNetServerContext = oneNetServer.getContexts().get(msg.getContextName());
                    if (oneNetServerContext == null) {
                        return;
                    }
                    log.info("OneNet data send to Internet.");
                    OneNetSession oneNetSession =
                            oneNetServerContext.getOneNetSessions().get(msg.getSessionId());
                    if (oneNetSession != null) {
                        byte[] outputData = msg.getData();
                        if (oneNetServerContext.getOneNetServerContextConfig().isAes()) {
                            outputData = WXBizMsgCrypt.getDecryptBytes(outputData);
                        }
                        if (oneNetServerContext.getOneNetServerContextConfig().isZip()) {
                            outputData = ByteZipUtil.unGzip(outputData);
                        }
                        oneNetSession.getInternetChannel().writeAndFlush(outputData);
                    } else {
                        log.info("Can't find exist session :" + msg.getSessionId());
                    }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
