package com.weyong.onenet.server;

import com.weyong.onenet.server.config.OneNetServerConfig;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.handler.OneNetChannelInitializer;
import com.weyong.onenet.server.session.OneNetConnectionManager;
import io.netty.bootstrap.ServerBootstrap;
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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
@Service
@EnableConfigurationProperties({OneNetServerConfig.class})
public class OneNetServer {
    private OneNetConnectionManager oneNetConnectionManager = new OneNetConnectionManager();
    private ConcurrentHashMap<String,OneNetServerContext> contexts = new ConcurrentHashMap<>();
    private ServerBootstrap insideBootstrap = new ServerBootstrap();
    public static EventLoopGroup bossGroup = new NioEventLoopGroup();
    public static EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Autowired
    public OneNetServer(OneNetServerConfig oneNetServerConfig){
        insideBootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new OneNetChannelInitializer(this))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        if(oneNetServerConfig!=null){
            startServer(oneNetServerConfig);
        }
    }

    public void startServer(OneNetServerConfig oneNetServerConfig){
        try {
            ChannelFuture channel = insideBootstrap.bind(oneNetServerConfig.getOneNetPort()).sync();
            log.info(String.format("Server OneNet port %d start success.",oneNetServerConfig.getOneNetPort()));
            oneNetServerConfig.getTcpContexts().stream().forEach((contextConfig)->{
                createContext(contextConfig);
            });
        } catch (InterruptedException e) {
            log.error(String.format("Server OneNet port %d start failed. The reason is :%s",oneNetServerConfig.getOneNetPort(),e.getMessage()));
        }
    }

    public void createContext(OneNetServerContextConfig oneNetContextConfig) {
            OneNetServerContext currentContext =contexts.computeIfAbsent(oneNetContextConfig.getContextName()
                    ,(contextName)->new OneNetServerContext(oneNetContextConfig,this));
    }
}
