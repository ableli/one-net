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

import java.util.Date;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
public class OneNetInboundHandler extends SimpleChannelInboundHandler<BasePackage> {
    private ServerSession serverSession;

    public OneNetInboundHandler(ServerSession serverSession){
        this.serverSession = serverSession;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        serverSession.invalidAllClientSessions();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasePackage msg) {
            switch (msg.getOpType()){
                case  BasePackage.HEART_BEAT:
                    serverSession.setLastHeartbeatTime(new Date());
                    break;
                case  BasePackage.INITIAL_RESPONSE:
                    serverSession.updateContextSettings((InitialResponsePackage)msg);
                    break;
                case BasePackage.INVALID_SESSION:
                    String oneNetName = msg.getContextName();
                    Long sessionId = msg.getSessionId();
                    if (StringUtils.isNotEmpty(oneNetName) && sessionId != null) {
                        OneNetClientContext context = serverSession.getOneNetClientContextMap().get(oneNetName);
                        context.getSessionMap().get(sessionId).closeFromOneNet();
                    }
                    break;
                case BasePackage.DATA:
                    if (StringUtils.isNotEmpty( msg.getContextName()) &&  msg.getSessionId() != null) {
                        OneNetClientContext context = serverSession.getOneNetClientContextMap().get( msg.getContextName());
                        ClientSession clientSession = context.
                                getSessionMap().computeIfAbsent(msg.getSessionId(), (id) -> {
                            ClientSession newClientSession = new ClientSession(id, serverSession, context, null);
                            try {
                                newClientSession.setLocalChannel(context.getContextLocalChannel(newClientSession));
                                return newClientSession;
                            }catch (Exception ex) {
                                log.error(ex.getMessage());
                                ctx.channel().writeAndFlush(
                                        new InvalidSessionPackage(
                                                newClientSession.getContextName(),
                                                id));
                            }
                            return null;
                        });
                            clientSession.getLocalChannel().writeAndFlush(((DataPackage)msg).getRawData());
                    }
                    break;

            }
    }
}
