package com.weyong.onenet.server.Initializer;

import com.weyong.codec.OneNetMsgDecoder;
import com.weyong.codec.OneNetMsgEncoder;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.handler.OneNetChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by hao.li on 2017/4/12.
 */
public class OneNetChannelInitializer extends ChannelInitializer<SocketChannel> {

    private OneNetServer oneNetServer;

    public OneNetChannelInitializer(OneNetServer oneNetServer) {
        this.oneNetServer = oneNetServer;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new OneNetMsgDecoder())
                .addLast(new OneNetMsgEncoder())
                .addLast(new OneNetChannelInboundHandler(this.oneNetServer));
    }
}
