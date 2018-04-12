package com.weyong.onenet.server.handler;

import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.context.OneNetServerHttpContext;
import com.weyong.onenet.server.session.OneNetHttpSession;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * Created by haoli on 12/26/2017.
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private OneNetServerHttpContext oneNetServerContext;

    public HttpChannelInitializer(OneNetServerHttpContext oneNetServerContext){
        this.oneNetServerContext = oneNetServerContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        OneNetHttpSession httpSession =(OneNetHttpSession)oneNetServerContext.createSession(ch,null);
            int bytePreSecond = oneNetServerContext.getOneNetServerContextConfig().getKBps() * 1024;
            ch.pipeline()
                    .addLast(new ChannelTrafficShapingHandler(bytePreSecond,
                            bytePreSecond))
                    .addLast(new HttpRawDataHandler(oneNetServerContext,httpSession))
                    .addLast(new HttpRequestHandler(oneNetServerContext,httpSession))
                    .addLast(new ByteArrayEncoder());

    }
}
