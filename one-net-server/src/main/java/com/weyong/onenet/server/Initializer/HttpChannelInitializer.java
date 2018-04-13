package com.weyong.onenet.server.Initializer;

import com.weyong.onenet.server.context.OneNetServerHttpContext;
import com.weyong.onenet.server.handler.HttpRawDataHandler;
import com.weyong.onenet.server.session.OneNetHttpSession;
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
        OneNetHttpSession httpSession = (OneNetHttpSession) OneNetServerHttpContext.instance.createSession(ch, null);
        int bytePreSecond = 5 * 1024;
        ch.pipeline()
                .addLast(trafficHandler, new ChannelTrafficShapingHandler(bytePreSecond,
                        bytePreSecond))
                .addLast(new HttpRawDataHandler(httpSession))
                //.addLast(new HttpRequestHandler(httpSession))
                .addLast(new ByteArrayEncoder());

    }
}
