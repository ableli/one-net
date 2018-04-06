package com.weyong.onenet.server.context;

import com.weyong.onenet.server.context.handler.InternetChannelInboundHandler;
import com.weyong.onenet.server.context.session.OneNetSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hao.li on 2017/4/12.
 */
@Slf4j
public class OneNetInternetChannelInitializer extends ChannelInitializer<SocketChannel> {
    private OneNetServerContext oneNetServerContext;

    public OneNetInternetChannelInitializer(OneNetServerContext oneNetServerContext){
        this.oneNetServerContext = oneNetServerContext;
    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Channel oneNetChannel =
                oneNetServerContext.getOneNetServer().getOneNetConnectionManager().getAvailableChannel(
                        oneNetServerContext.getOneNetServerContextConfig().getContextName());
        if(oneNetChannel == null){
            ch.close();
        }else {
            OneNetSession oneNetSession =oneNetServerContext.createSession(ch ,oneNetChannel);
            ch.pipeline()
                    .addLast(new InternetChannelInboundHandler(oneNetSession))
                    .addLast(new ByteArrayEncoder());
        }
    }
}
