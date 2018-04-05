package com.weyong.onenet.initializer;

import com.weyong.onenet.handler.LocalInboudHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by hao.li on 2017/4/13.
 */
public class LocalChannelInitializer  extends ChannelInitializer<SocketChannel> {
    public static final String LOCAL_RESPONSE_HANDLER = "LocalResponseHandler";
    private String outsideChannelId;

    public  LocalChannelInitializer(String channelId){
        this.outsideChannelId = channelId;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(LOCAL_RESPONSE_HANDLER,new LocalInboudHandler(outsideChannelId))
                .addLast(new ByteArrayEncoder());
    }
}
