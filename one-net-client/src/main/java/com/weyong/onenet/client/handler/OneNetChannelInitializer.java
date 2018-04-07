package com.weyong.onenet.client.handler;

import com.weyong.codec.OneNetMsgDecoder;
import com.weyong.codec.OneNetMsgEncoder;
import com.weyong.onenet.client.session.ServerSession;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by hao.li on 2017/4/13.
 */
public class OneNetChannelInitializer extends ChannelInitializer<SocketChannel> {
    ServerSession serverSession;
    public OneNetChannelInitializer(ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
//                p.addLast(new ObjectEncoder())
//                .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
                p.addLast(new LoggingHandler(LogLevel.DEBUG))
                        .addLast(new OneNetMsgEncoder())
                                .addLast(new OneNetMsgDecoder())
                                        .addLast(new OneNetInboundHandler(serverSession));
    }
}
