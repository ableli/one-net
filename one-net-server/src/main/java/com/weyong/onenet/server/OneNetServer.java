package com.weyong.onenet.server;

import com.weyong.onenet.server.Initializer.OneNetChannelInitializer;
import com.weyong.onenet.server.config.OneNetServerConfig;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.config.OneNetServerHttpContextConfig;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.context.OneNetServerHttpContextHolder;
import com.weyong.onenet.server.manager.OneNetTcpConnectionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
@Service
@EnableConfigurationProperties({OneNetServerConfig.class})
public class OneNetServer {
    public static EventLoopGroup bossGroup = new NioEventLoopGroup();
    public static EventLoopGroup workerGroup = new NioEventLoopGroup();
    private OneNetTcpConnectionManager oneNetTcpConnectionManager = new OneNetTcpConnectionManager();
    private ConcurrentHashMap<String, OneNetServerContext> contexts = new ConcurrentHashMap<>();
    private ServerBootstrap insideBootstrap = new ServerBootstrap();
    private OneNetServerConfig oneNetServerConfig;

    @Autowired
    public OneNetServer(OneNetServerConfig oneNetServerConfig) {
        this.oneNetServerConfig = oneNetServerConfig;
        insideBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new OneNetChannelInitializer(this))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        if (oneNetServerConfig != null) {
            startServer(oneNetServerConfig);
        }
    }

    public void startServer(OneNetServerConfig oneNetServerConfig) {
        try {
            log.info(String.format("Server OneNet port %d start success.", oneNetServerConfig.getOneNetPort()));
            ChannelFuture channel = insideBootstrap.bind(oneNetServerConfig.getOneNetPort()).sync();
            if (!CollectionUtils.isEmpty(oneNetServerConfig.getTcpContexts())) {
                log.info(String.format("Start Tcp Contexts, size is : %d", oneNetServerConfig.getTcpContexts().size()));
                oneNetServerConfig.getTcpContexts().stream().forEach((contextConfig) -> {
                    createContext(contextConfig);
                });
            }
            if (!CollectionUtils.isEmpty(oneNetServerConfig.getHttpContexts())) {
                log.info(String.format("Start Http Contexts, size is : %d", oneNetServerConfig.getHttpContexts().size()));
                OneNetServerHttpContextHolder httpContext = OneNetServerHttpContextHolder.instance(oneNetServerConfig.getHttpContexts(), this);
                oneNetServerConfig.getHttpContexts().stream().forEach((httpConfig)->{
                    OneNetServerHttpContextHolder.instance.add(createHttpContext(httpConfig));
                });
            }
        } catch (InterruptedException e) {
            log.error(String.format("Server OneNet port %d start failed. The reason is :%s", oneNetServerConfig.getOneNetPort(), e.getMessage()));
        }
    }

    private OneNetServerContext createHttpContext(OneNetServerHttpContextConfig contextConfig) {
           return contexts.computeIfAbsent(contextConfig.getContextName()
                    , (contextName) -> new OneNetServerContext(contextConfig,oneNetTcpConnectionManager));
    }

    public void createContext(OneNetServerContextConfig oneNetContextConfig) {
        contexts.computeIfAbsent(oneNetContextConfig.getContextName()
                , (contextName) -> new OneNetServerContext(oneNetContextConfig, oneNetTcpConnectionManager));
    }

    public void closeSessions(Channel channel) {
        getContexts().values().stream().forEach((oneNetServerContext) -> {
            oneNetServerContext.getOneNetSessions().values().stream()
                    .filter((oneNetSession) -> {
                            return oneNetSession.getOneNetChannel() == channel;}
                            ).forEach((toCloseSession) -> {
                            oneNetServerContext.close(toCloseSession);
                    }
            );
        });
    }
}
