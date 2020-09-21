package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.context.OneNetClientContext;
import com.weyong.onenet.client.session.ClientSession;
import com.weyong.onenet.client.session.ServerSession;
import com.weyong.onenet.dto.BasePackage;
import com.weyong.onenet.dto.DataPackage;
import com.weyong.onenet.dto.InitialResponsePackage;
import com.weyong.onenet.dto.InvalidSessionPackage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
public class OneNetInboundHandler extends SimpleChannelInboundHandler<BasePackage> {
    private ServerSession serverSession;

    public OneNetInboundHandler(ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        serverSession.invalidAllClientSessions();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasePackage msg) {
        switch (msg.getMsgType()) {
            case BasePackage.HEART_BEAT:
                serverSession.setLastHeartbeatTime(Instant.now());
                break;
            case BasePackage.INITIAL_RESPONSE:
                serverSession.updateContextSettings((InitialResponsePackage) msg);
                break;
            case BasePackage.INVALID_SESSION:
                String oneNetName = msg.getContextName();
                Long sessionId = msg.getSessionId();
                //log.info(String.format("context %s session %d closed from server.",oneNetName,sessionId));
                if (StringUtils.isNotEmpty(oneNetName) && sessionId != null) {
                    OneNetClientContext context = serverSession.getOneNetClientContextMap().get(oneNetName);
                    context.close(sessionId);
                }
                break;
            case BasePackage.DATA:
                if (StringUtils.isNotEmpty(msg.getContextName()) && msg.getSessionId() != null) {
                    OneNetClientContext context = serverSession.getOneNetClientContextMap().get(msg.getContextName());
                    if (context == null) {
                        ctx.channel().writeAndFlush(
                                new InvalidSessionPackage(
                                        msg.getContextName(),
                                        msg.getSessionId()));
                        return;
                    }
                    ClientSession clientSession = context.getSessionMap().get(msg.getSessionId());
                    if (clientSession != null) {
                        clientSession.getLocalChannel().writeAndFlush(((DataPackage) msg).getRawData());
                    } else {
                        CompletableFuture.runAsync(() -> {
                            ClientSession newSession = context.getCurrentSession(serverSession, msg.getSessionId());
                            if (newSession == null) {
                                log.info("Local Channel can't create from pool.");
                                ctx.channel().writeAndFlush(
                                        new InvalidSessionPackage(
                                                msg.getContextName(),
                                                msg.getSessionId()));
                            } else {
                                newSession.getLocalChannel().writeAndFlush(((DataPackage) msg).getRawData());
                            }
                        });
                    }
                }
                break;
                default: throw new RuntimeException("Not Supported Package Type");
        }
    }
}
