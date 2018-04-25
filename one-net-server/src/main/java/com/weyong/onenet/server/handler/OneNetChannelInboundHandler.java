package com.weyong.onenet.server.handler;

import com.weyong.onenet.dto.*;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.session.ClientSession;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;

/**
 * Author by haoli on 2017/4/12.
 */
@Slf4j
public class OneNetChannelInboundHandler extends SimpleChannelInboundHandler<BasePackage> {
    private ClientSession clientSession;
    private OneNetServer oneNetServer;

    public OneNetChannelInboundHandler(OneNetServer oneNetServer) {
        this.oneNetServer = oneNetServer;
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        ctx.close();
        if (clientSession != null && StringUtils.isNotEmpty(this.clientSession.getClientName())) {
            log.info(String.format("Client session %s inactive.", clientSession.getClientName()));
            this.clientSession.setClientChannel(null);
        }
        oneNetServer.closeSessionsByClient(ctx.channel());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasePackage msg) throws Exception {
        try {
            switch (msg.getMsgType()) {
                case BasePackage.HEART_BEAT:
                    log.debug("HEART_BEAT at"+ctx.channel().id().asShortText());
                    ctx.channel().writeAndFlush(msg);
                    this.clientSession.setLastHeartBeatTime(new Date());
                    break;
                case BasePackage.INITIAL_REQUEST:
                    log.debug("INITIAL_REQUEST at"+ctx.channel().id().asShortText());
                    synchronized (this) {
                        InitialRequestPackage requestMsg = (InitialRequestPackage) msg;
                        if (CollectionUtils.isEmpty(requestMsg.getContextNames())) {
                            ctx.close();
                            break;
                        }
                        log.info(String.format("Client %s with contexts [%s] request connections.",
                                requestMsg.getClientName(), StringUtils.join(requestMsg.getContextNames(), ",")));
                        clientSession = new ClientSession(requestMsg.getClientName(), ctx.channel(),new Date());
                        requestMsg.getContextNames().stream().forEach((contextName) -> {
                            if (!oneNetServer.getContexts().containsKey(contextName)) {
                                ctx.channel().writeAndFlush(new MessagePackage(
                                        String.format("OneNet %s's config not found in Server", contextName)));
                            } else {
                                OneNetServerContextConfig config = oneNetServer.getContexts().get(contextName).getOneNetServerContextConfig();
                                ctx.channel().writeAndFlush(new InitialResponsePackage(contextName, config.isZip(), config.isAes(), config.getKBps()));
                                oneNetServer.getOneNetTcpConnectionManager().registerClientSession(
                                        contextName,requestMsg.getClientName(), clientSession);
                            }
                        });
                    }
                    break;
                case BasePackage.INVALID_SESSION:
                    log.debug("INVALID_SESSION at"+ctx.channel().id().asShortText());
                    OneNetServerContext oneNetServerContext = oneNetServer.getContexts().get(msg.getContextName());
                    if (oneNetServerContext != null) {
                        oneNetServerContext.close(msg.getSessionId());
                    }
                    break;
                case BasePackage.DATA:
                    log.debug("DATA at"+ctx.channel().id().asShortText());
                    oneNetServerContext = oneNetServer.getContexts().get(msg.getContextName());
                    if (oneNetServerContext == null) {
                        return;
                    };
                    OneNetSession oneNetSession =
                            oneNetServerContext.getOneNetSessions().get(msg.getSessionId());
                    if (oneNetSession != null) {
                        DataPackage dataPackage = (DataPackage) msg;
                        byte[] outputData = dataPackage.getRawData();
                        oneNetSession.getInternetChannel().writeAndFlush(outputData);
                    } else {
                        if(oneNetSession.getClientSession()!=null && oneNetSession.getClientSession().getClientChannel()!=null) {
                            oneNetSession.getClientSession().getClientChannel().writeAndFlush(new InvalidSessionPackage(msg.getContextName(), msg.getSessionId()));
                            log.info("Can't find exist session :" + msg.getSessionId());
                        }
                    }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
