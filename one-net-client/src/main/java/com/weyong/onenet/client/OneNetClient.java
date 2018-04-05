package com.weyong.onenet.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
public class OneNetClient {
    private static Bootstrap b;

    public  OneNetClient() throws Exception {
        b = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);

    }

    public static Channel createChannel(final String ip, final int port,ChannelInitializer channelInitializer) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = b.handler(channelInitializer).connect(ip,port).sync();
            return channelFuture.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
