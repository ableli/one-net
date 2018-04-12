package com.weyong.onenet.server.handler;

import com.weyong.onenet.dto.*;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.session.OneNetSession;
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
public class OneNetChannelInboundHandler extends SimpleChannelInboundHandler<BasePackage> {
    private OneNetSession oneNetSession;

    public OneNetChannelInboundHandler(OneNetSession oneNetSession){
        this.oneNetSession = oneNetSession;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        ctx.close();
        if(StringUtils.isNotEmpty(this.oneNetSession.getClientName())) {
            this.oneNetServer.getOneNetConnectionManager().removeOneNetSession("clientName");
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasePackage msg) throws Exception {
        try {
            switch (msg.getOpType()) {
                case BasePackage.HEART_BEAT:
                    ctx.channel().writeAndFlush(msg);
                    break;
                case BasePackage.INITIAL_REQUEST:
                    InitialRequestPackage requestMsg = (InitialRequestPackage) msg;
                    if (CollectionUtils.isEmpty(requestMsg.getContextNames())) {
                        ctx.close();
                        break;
                    }
                    log.info(String.format("Client %s with tcpContexts [%s] request connections.",
                            requestMsg.getClientName(), StringUtils.join(requestMsg.getContextNames(), ",")));
                    List<String> toRemoveContextName = new ArrayList<>();

                    requestMsg.getContextNames().stream().forEach((contextName) -> {
                        if (!oneNetServer.getContexts().containsKey(contextName)) {
                            ctx.channel().writeAndFlush(new MessagePackage(
                                    String.format("OneNet %s's config not found in Server", contextName)));
                            toRemoveContextName.add(contextName);
                        }else{
                            OneNetServerContextConfig config = oneNetServer.getContexts().get(contextName).getOneNetServerContextConfig();
                            ctx.channel().writeAndFlush(new InitialResponsePackage(contextName,config.isZip(),config.isAes(),config.getKBps()));
                        }
                    });
                    requestMsg.getContextNames().removeAll(toRemoveContextName);
                    if (CollectionUtils.containsAny(Collections.list(oneNetServer.getContexts().keys()),
                            requestMsg.getContextNames())) {
                        this.clientName = requestMsg.getClientName();
                        this.oneNetServer.getOneNetConnectionManager().refreshSessionChannel(
                                requestMsg.getClientName(), requestMsg.getContextNames(), ctx.channel());
                    }
                    break;
                case BasePackage.INVALID_SESSION:
                    OneNetServerContext oneNetServerContext = oneNetServer.getContexts().get(msg.getContextName());
                    if (oneNetServerContext != null) {
                        OneNetSession session = oneNetServerContext.getOneNetSessions().get(msg.getSessionId());
                        if(session != null) {
                            session.closeFromOneNet();
                        }
                    }
                    break;
                case BasePackage.DATA:
                    oneNetServerContext = oneNetServer.getContexts().get(msg.getContextName());
                    if (oneNetServerContext == null) {
                        return;
                    };
                    OneNetSession oneNetSession =
                            oneNetServerContext.getOneNetSessions().get(msg.getSessionId());
                    if (oneNetSession != null) {
                        DataPackage dataPackage = (DataPackage)msg;
                        byte[] outputData = dataPackage.getRawData();
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
