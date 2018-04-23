package com.weyong.onenet.client.initializer;

import com.weyong.codec.OneNetMsgDecoder;
import com.weyong.codec.OneNetMsgEncoder;
import com.weyong.onenet.client.handler.OneNetInboundHandler;
import com.weyong.onenet.client.session.ServerSession;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.WriteTimeoutHandler;

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
        ch.pipeline().addLast(new WriteTimeoutHandler(5))
                .addLast(new OneNetMsgEncoder())
                .addLast(new OneNetMsgDecoder())
                .addLast(new OneNetInboundHandler(serverSession));
    }
}
