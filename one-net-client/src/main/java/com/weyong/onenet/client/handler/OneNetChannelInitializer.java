package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.session.ServerSession;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

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
                p.addLast(new ObjectEncoder())
                .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
        .addLast(new OneNetInboundHandler(serverSession));
    }
}
