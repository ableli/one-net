package com.weyong.onenet.client;

import com.weyong.onenet.client.config.OneNetClientConfig;
import com.weyong.onenet.client.config.OnenetClientServerConfig;
import com.weyong.onenet.client.serverSession.OneNetServerSessionManager;
import com.weyong.onenet.client.serverSession.ServerSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
@Service
@EnableConfigurationProperties({OneNetClientConfig.class})
public class OneNetClient {
    private static Bootstrap b;
    @Autowired
    private OneNetServerSessionManager oneNetServerSessionManager;

    @Autowired
    public  OneNetClient(OneNetClientConfig oneNetClientConfig) throws Exception {
        b = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);
        if(!CollectionUtils.isEmpty(oneNetClientConfig.getServerConfigs())){
            oneNetClientConfig.getServerConfigs().stream().forEach(
                    (onenetClientServerConfig) ->
                        oneNetServerSessionManager.getOneNetServerSessions()
                                .putIfAbsent(oneNetClientConfig.getServerName(),
                                        new ServerSession(onenetClientServerConfig))
            );
        }
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
