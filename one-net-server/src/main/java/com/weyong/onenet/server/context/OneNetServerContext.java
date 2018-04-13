package com.weyong.onenet.server.context;

import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.Initializer.InternetChannelInitializer;
import com.weyong.onenet.server.manager.OneNetConnectionManager;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
@NoArgsConstructor
public class OneNetServerContext {
    protected ServerBootstrap outsideBootstrap = new ServerBootstrap();
    private Map<Long, OneNetSession> oneNetSessions= new HashMap<>();
    private OneNetConnectionManager oneNetConnectionManager;
    private OneNetServerContextConfig oneNetServerContextConfig;

    public OneNetServerContext(OneNetServerContextConfig oneNetServerContextConfig, OneNetServer oneNetServer) {
        this.oneNetServerContextConfig = oneNetServerContextConfig;
        this.setOneNetConnectionManager(oneNetServer.getOneNetTcpConnectionManager());
        outsideBootstrap.group(OneNetServer.bossGroup,OneNetServer.workerGroup);
        outsideBootstrap.channel(NioServerSocketChannel.class)
                .childHandler(new InternetChannelInitializer(this))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            if(oneNetServerContextConfig.getInternetPort().intValue()==80){
                 OneNetServerHttpContext.tcp80Initialed = true;
            }
            outsideBootstrap.bind(oneNetServerContextConfig.getInternetPort()).sync();
            log.info(String.format("OneNet Server Context: %s started, Port: %d",
                    oneNetServerContextConfig.getContextName(),
                    oneNetServerContextConfig.getInternetPort()));
        }catch (InterruptedException ex){
            log.error(String.format("Start Context %s failed. ex is : %s",
                    oneNetServerContextConfig.getContextName(),
                    ex.getMessage()));
        }
    }

    public OneNetSession getSession(Long sessionId) {
        return oneNetSessions.get(sessionId);
    }

    public OneNetSession createSession(SocketChannel ch, Channel oneNetChannel) {
        OneNetSession oneNetSession = new OneNetSession(this, ch, oneNetChannel);
        oneNetSessions.put(oneNetSession.getSessionId(), oneNetSession);
        return oneNetSession;
    }

    public OneNetServerContextConfig getOneNetServerContextConfig(String name){
        return oneNetServerContextConfig;
    }
}
