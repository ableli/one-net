package com.weyong.onenet.server.Initializer;

import com.weyong.onenet.server.handler.HttpChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * Created by haoli on 12/26/2017.
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    public static final String trafficHandler = "Traffic";

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        int bytePreSecond = 5 * 1024;
        ch.pipeline()
                .addLast(trafficHandler, new ChannelTrafficShapingHandler(bytePreSecond,
                        bytePreSecond))
                .addLast(new HttpChannelInboundHandler())
                .addLast(new ByteArrayEncoder());

    }
}
