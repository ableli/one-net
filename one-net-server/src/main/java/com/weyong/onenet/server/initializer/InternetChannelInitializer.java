package com.weyong.onenet.server.initializer;

import com.weyong.constants.OneNetCommonConstants;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.handler.InternetChannelInboundHandler;
import com.weyong.onenet.server.session.ClientSession;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hao.li on 2017/4/12.
 */
@Slf4j
public class InternetChannelInitializer extends ChannelInitializer<SocketChannel> {
    private OneNetServerContext oneNetServerContext;

    public InternetChannelInitializer(OneNetServerContext oneNetServerContext) {
        this.oneNetServerContext = oneNetServerContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ClientSession clientSession =
                oneNetServerContext.getOneNetConnectionManager().getAvailableSession(
                        oneNetServerContext.getOneNetServerContextConfig().getContextName());
        if (!clientSession.isActive()) {
            ch.close();
        } else {
            OneNetSession oneNetSession = oneNetServerContext.createSession(ch, clientSession);
            int bytePreSecond = oneNetServerContext.getOneNetServerContextConfig().getKBps() * OneNetCommonConstants.KByte;
            ch.pipeline()
                    .addLast(new ChannelTrafficShapingHandler(bytePreSecond,
                            bytePreSecond))
                    .addLast(new InternetChannelInboundHandler(oneNetServerContext, oneNetSession))
                    .addLast(new ByteArrayEncoder());
        }
    }
}
