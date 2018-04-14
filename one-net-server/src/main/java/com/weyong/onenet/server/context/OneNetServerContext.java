package com.weyong.onenet.server.context;

import com.weyong.onenet.server.Initializer.InternetChannelInitializer;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.manager.OneNetConnectionManager;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
@NoArgsConstructor
public class OneNetServerContext {
    private Map<Long, OneNetSession> oneNetSessions = new HashMap<>();
    private OneNetConnectionManager oneNetConnectionManager;
    private OneNetServerContextConfig oneNetServerContextConfig;

    public OneNetServerContext(OneNetServerContextConfig oneNetServerContextConfig, OneNetConnectionManager oneNetConnectionManager) {
        this.oneNetServerContextConfig = oneNetServerContextConfig;
        this.oneNetConnectionManager = oneNetConnectionManager;
        if (oneNetServerContextConfig.getInternetPort().intValue() == 80) {
            OneNetServerHttpContextHolder.tcp80Initialed = true;
        }
        ServerBootstrap outsideBootstrap = new ServerBootstrap();
        outsideBootstrap.group(OneNetServer.bossGroup, OneNetServer.workerGroup);
        outsideBootstrap.channel(NioServerSocketChannel.class)
                .childHandler(new InternetChannelInitializer(this))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            outsideBootstrap.bind(oneNetServerContextConfig.getInternetPort()).sync();
            log.info(String.format("OneNet Server Context: %s started, Port: %d",
                    oneNetServerContextConfig.getContextName(),
                    oneNetServerContextConfig.getInternetPort()));
        } catch (InterruptedException ex) {
            log.error(String.format("Start Context %s failed. ex is : %s",
                    oneNetServerContextConfig.getContextName(),
                    ex.getMessage()));
        }
    }

    public OneNetSession getSession(Long sessionId) {
        return oneNetSessions.get(sessionId);
    }

    public OneNetSession createSession(Channel ch, Channel oneNetChannel) {
        OneNetSession oneNetSession = new OneNetSession(this.getOneNetServerContextConfig().getContextName(), ch, oneNetChannel);
        oneNetSessions.put(oneNetSession.getSessionId(), oneNetSession);
        return oneNetSession;
    }

    public OneNetServerContextConfig getOneNetServerContextConfig(String name) {
        return oneNetServerContextConfig;
    }

    @Autowired
    public String toString(){
        return oneNetServerContextConfig.getContextName();
    }

    public void close(OneNetSession oneNetSession) {
        log.debug(String.format("Session %d closed. Context alive session count is : %d",oneNetSession.getSessionId(), this.getOneNetSessions().size()));
        oneNetSession.close();
        this.getOneNetSessions().remove(oneNetSession.getSessionId());
    }

    public void close(Long sessionId) {
        log.debug(String.format("Session %d closed. Context alive session count is : %d",sessionId, this.getOneNetSessions().size()));
        OneNetSession oneNetSession = this.getOneNetSessions().remove(sessionId);
        if(oneNetSession!=null) {
            oneNetSession.close();
        }
    }

    public Channel getAvailableChannel() {
        return this.getOneNetConnectionManager().getAvailableChannel(this.getOneNetServerContextConfig().getContextName());
    }
}
