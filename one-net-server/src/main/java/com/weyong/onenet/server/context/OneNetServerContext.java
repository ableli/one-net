package com.weyong.onenet.server.context;

import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.context.session.OneNetSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
public class OneNetServerContext {
    private static ServerBootstrap outsideBootstrap = new ServerBootstrap();
    private Map<Long, OneNetSession> oneNetSessions= new HashMap<>();
    private OneNetServer oneNetServer;
    private OneNetServerContextConfig oneNetServerContextConfig;
    public OneNetServerContext(OneNetServerContextConfig oneNetServerContextConfig, OneNetServer oneNetServer) {
        this.oneNetServer = oneNetServer;
        this.oneNetServerContextConfig = oneNetServerContextConfig;
        outsideBootstrap.group(oneNetServer.getBossGroup(),oneNetServer.getWorkerGroup()).channel(NioServerSocketChannel.class)
                .childHandler(new OneNetInternetChannelInitializer(this))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        log.info(String.format("OneNet Server Context: %s started, Port: %d",
                oneNetServerContextConfig.getContextName(),
                oneNetServerContextConfig.getInternetPort()));
    }

    public OneNetSession removeSession(Long sessionId) {
        return oneNetSessions.remove(sessionId);
    }

    public OneNetSession getSession(Long sessionId) {
        return oneNetSessions.get(sessionId);
    }

    public OneNetSession createSession(SocketChannel ch, Channel oneNetChannel) {
       OneNetSession oneNetSession = new OneNetSession(this, ch, oneNetChannel);
       oneNetSessions.put(oneNetSession.getSessionId(), oneNetSession);
       return oneNetSession;
    }

    public void closeSession(Long sessionId) {
        if(sessionId != null && oneNetSessions.containsKey(sessionId)){
            oneNetSessions.remove(sessionId).close(true);
        }
    }
}
