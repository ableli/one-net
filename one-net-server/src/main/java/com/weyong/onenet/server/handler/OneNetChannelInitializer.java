package com.weyong.onenet.server.handler;

import com.weyong.codec.OneNetMsgDecoder;
import com.weyong.codec.OneNetMsgEncoder;
import com.weyong.onenet.server.OneNetServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by hao.li on 2017/4/12.
 */
public class OneNetChannelInitializer extends ChannelInitializer<SocketChannel> {

    private OneNetServer oneNetServer ;

    public OneNetChannelInitializer(OneNetServer oneNetServer) {
        this.oneNetServer = oneNetServer;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
//                .addLast(new LoggingHandler(LogLevel.DEBUG))
//                .addLast(new ObjectEncoder())
//                .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
                .addLast(new OneNetMsgDecoder())
                        .addLast(new OneNetMsgEncoder())
        .addLast(new OneNetChannelInboundHandler(this.oneNetServer));
    }
}
