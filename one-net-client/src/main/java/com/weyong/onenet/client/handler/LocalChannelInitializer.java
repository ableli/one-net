package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.context.OneNetClientContext;
import com.weyong.onenet.client.session.ClientSession;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * Created by hao.li on 2017/4/13.
 */
public class LocalChannelInitializer extends ChannelInitializer<SocketChannel> {
    public static final String LOCAL_RESPONSE_HANDLER = "LocalResponseHandler";
    public static final String CHANNEL_TRAFFIC_HANDLER = "ChannelTrafficHandler";
    private ClientSession clientSession;
    private OneNetClientContext oneNetClientContext;

    public LocalChannelInitializer(OneNetClientContext oneNetClientContext, ClientSession clientSession) {
        this.clientSession = clientSession;
        this.oneNetClientContext = oneNetClientContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        int bytesPreSecond = 0;
        if (clientSession != null) {
            bytesPreSecond = oneNetClientContext.getKBps() * 1024;
        }
        p.addLast(CHANNEL_TRAFFIC_HANDLER, new ChannelTrafficShapingHandler(bytesPreSecond,
                bytesPreSecond))
                .addLast(LOCAL_RESPONSE_HANDLER, new LocalInboudHandler(oneNetClientContext,clientSession))
                .addLast(new ByteArrayEncoder());
    }
}
