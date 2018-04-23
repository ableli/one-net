package com.weyong.onenet.client;

import com.weyong.onenet.client.config.OneNetClientConfig;
import com.weyong.onenet.client.manager.OneNetServerSessionManager;
import com.weyong.onenet.client.session.ServerSession;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
@Service
@Data
@EnableConfigurationProperties({OneNetClientConfig.class})
@EnableScheduling
public class OneNetClient {
    private static Bootstrap bootstrap;
    private String clientName;

    @Autowired
    public OneNetClient(OneNetServerSessionManager oneNetServerSessionManager, OneNetClientConfig oneNetClientConfig) throws Exception {
        bootstrap = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);
        oneNetServerSessionManager.setClientName(oneNetClientConfig.getServerName());
        oneNetServerSessionManager.setReconnectAfterNSeconds(oneNetClientConfig.getReconnectSeconds());
        if (!CollectionUtils.isEmpty(oneNetClientConfig.getServerConfigs())) {
            clientName = oneNetClientConfig.getServerName();
            oneNetClientConfig.getServerConfigs().stream().forEach(
                    (onenetClientServerConfig) ->
                            oneNetServerSessionManager.getOneNetServerSessions()
                                    .putIfAbsent(oneNetClientConfig.getServerName(),
                                            new ServerSession(onenetClientServerConfig))
            );
        }
    }

    public static Channel createChannel(final String ip, final int port, ChannelInitializer channelInitializer) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.handler(channelInitializer).connect(ip, port);
            if(channelFuture.await(200,TimeUnit.MILLISECONDS)) {
                return channelFuture.channel();
            }else{
                log.error(String.format("get local channel %s:%d failed",ip,port));
                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
