package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.OneNetClientContext;
import com.weyong.onenet.client.clientSession.ClientSession;
import com.weyong.onenet.client.serverSession.ServerSession;
import com.weyong.onenet.dto.DataTransfer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
public class OneNetInboundHandler extends SimpleChannelInboundHandler<DataTransfer> {
    private ServerSession serverSession;

    public OneNetInboundHandler(ServerSession serverSession){
        this.serverSession = serverSession;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataTransfer msg) {
        if(DataTransfer.OP_TYPE_HEART_BEAT == msg.getOpType()){
            serverSession.setLastHeartbeatTime(new Date());
        }else {
            String oneNetName = msg.getContextName();
            Long sessionId = msg.getSessionId();
            if (StringUtils.isNotEmpty(oneNetName) && sessionId != null) {
                OneNetClientContext context = serverSession.getOneNetClientContextMap().get(oneNetName);
                ClientSession clientSession = context.
                        getSessionMap().computeIfAbsent(sessionId, id -> {
                    ClientSession newClientSession = new ClientSession(id, ctx.channel(),context ,null);
                    newClientSession.setLocalChannel(context.getContextLocalChannel(newClientSession));
                    return  newClientSession;
                });
                if (msg.getOpType() != 0) {
                    if (DataTransfer.OP_TYPE_CLOSE == msg.getOpType()) {
                        context.removeSession(sessionId);
                    }
                } else {
                    clientSession.getLocalChannel().writeAndFlush(msg.getData());
                }
            }
        }
    }
}
