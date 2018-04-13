package com.weyong.onenet.client.handler;

import com.weyong.codec.OneNetMsgDecoder;
import com.weyong.codec.OneNetMsgEncoder;
import com.weyong.onenet.client.session.ServerSession;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

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
        ChannelPipeline p = ch.pipeline()
//                .addLast(new ObjectEncoder())
//                .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
//                    addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new OneNetMsgEncoder())
                .addLast(new OneNetMsgDecoder())
                .addLast(new OneNetInboundHandler(serverSession));
    }
}
