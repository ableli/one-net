package com.weyong.onenet.server.context;

import com.weyong.onenet.server.initializer.HttpChannelInitializer;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerHttpContextConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by hao.li on 4/10/2018.
 */
@Slf4j
@Data
public class OneNetServerHttpContextHolder {
    public static boolean tcp80Initialed = false;
    public static OneNetServerHttpContextHolder instance;
    protected ServerBootstrap outsideBootstrap = new ServerBootstrap();
    private Map<String, OneNetServerContext> hostnameLookupMap = new ConcurrentHashMap<>();
    private Map<OneNetServerHttpContextConfig, OneNetServerContext> contextsMap = new ConcurrentHashMap<>();

    private OneNetServerHttpContextHolder() {
        if (!tcp80Initialed) {
            outsideBootstrap.group(OneNetServer.bossGroup, OneNetServer.workerGroup);
            outsideBootstrap.channel(NioServerSocketChannel.class)
                    .childHandler(new HttpChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            try {
                outsideBootstrap.bind(80).sync();
                tcp80Initialed = true;
            } catch (InterruptedException ex) {
                log.error(String.format("Start Context %s failed. ex is : %s",
                        "Http",
                        ex.getMessage()));
            }
        }
    }

    public static OneNetServerHttpContextHolder instance(List<OneNetServerHttpContextConfig> oneNetServerContextConfigs, OneNetServer oneNetServer) {
        if (instance == null) {
            instance = new OneNetServerHttpContextHolder();
        }
        return instance;
    }

    public OneNetServerContext getContext(String hostName) {
        return hostnameLookupMap.computeIfAbsent(hostName, (name) -> getContextByHostname(name));
    }

    public void add(OneNetServerContext httpContext) {
        contextsMap.putIfAbsent((OneNetServerHttpContextConfig) httpContext.getOneNetServerContextConfig(), httpContext);
    }

    public OneNetServerContext getContextByHostname(String hostName) {
        Optional<Map.Entry<OneNetServerHttpContextConfig, OneNetServerContext>> optional = contextsMap.entrySet().stream().filter((entry) -> {
            return entry.getKey().getDomainRegExs().stream().anyMatch((regex) -> {
                return Pattern.compile(regex).matcher(hostName).matches();
            });
        }).findFirst();
        if (optional.isPresent()) {
            return optional.get().getValue();
        }
        return null;
    }
}
