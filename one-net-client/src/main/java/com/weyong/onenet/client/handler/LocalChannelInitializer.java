package com.weyong.onenet.client.handler;

import com.weyong.onenet.client.session.ClientSession;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by hao.li on 2017/4/13.
 */
public class LocalChannelInitializer  extends ChannelInitializer<SocketChannel> {
    public static final String LOCAL_RESPONSE_HANDLER = "LocalResponseHandler";
    private ClientSession clientSession;

    public  LocalChannelInitializer(ClientSession clientSession){
        this.clientSession = clientSession;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(LOCAL_RESPONSE_HANDLER,new LocalInboudHandler(clientSession))
                .addLast(new ByteArrayEncoder());
    }
}
