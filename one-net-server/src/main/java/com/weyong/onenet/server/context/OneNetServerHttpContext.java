package com.weyong.onenet.server.context;

import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.config.OneNetServerHttpContextConfig;
import com.weyong.onenet.server.Initializer.HttpChannelInitializer;
import com.weyong.onenet.server.session.OneNetHttpSession;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.OpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by hao.li on 4/10/2018.
 */
@Slf4j
public class OneNetServerHttpContext extends OneNetServerContext {
    public static boolean tcp80Initialed = false;
    public static OneNetServerHttpContext instance;
    private List<OneNetServerHttpContextConfig> oneNetServerContextConfigs;
    private Map<String,OneNetServerHttpContextConfig> fastLookupMap = new ConcurrentHashMap<>();
    private OneNetServerHttpContext(List<OneNetServerHttpContextConfig> oneNetServerContextConfigs){
        super();
        if(!tcp80Initialed) {
            this.oneNetServerContextConfigs = oneNetServerContextConfigs;
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
        log.info(String.format("OneNet Server Http Context: %s started, domain regex: [%s]",
                "Http",
                StringUtils.join(oneNetServerContextConfigs,",")));
    }

    public static OneNetServerHttpContext instance(List<OneNetServerHttpContextConfig> oneNetServerContextConfigs, OneNetServer oneNetServer) {
        if(instance == null){
            instance = new OneNetServerHttpContext(oneNetServerContextConfigs);
            instance.setOneNetConnectionManager(oneNetServer.getOneNetHttpConnectionManager());
        }
        return instance;
    }

    @Override
    public OneNetSession createSession(SocketChannel ch, Channel oneNetChannel) {
        OneNetHttpSession oneNetSession = new OneNetHttpSession(this, ch, oneNetChannel);
        this.getOneNetSessions().put(oneNetSession.getSessionId(), oneNetSession);
        return oneNetSession;
    }

    public OneNetServerHttpContextConfig getContextConfig(String hostName) {
        return fastLookupMap.computeIfAbsent(hostName,(name)->lookupContextConfig(name));
    }

    private OneNetServerHttpContextConfig lookupContextConfig(String name) {
        Optional<OneNetServerHttpContextConfig> target =  oneNetServerContextConfigs.stream().filter((httpContextConfg)->{
            return httpContextConfg.getDomainRegExs().stream().anyMatch((regex)->{
                return Pattern.compile(regex).matcher(name).matches();
            });
        }).findFirst();
        if(target.isPresent()){
            return target.get();
        }else{
            return null;
        }
    }

    @Override
    public OneNetServerContextConfig getOneNetServerContextConfig(String name){
        Optional<OneNetServerHttpContextConfig> optional =  oneNetServerContextConfigs.stream().filter((config)->{
            return name.equals(config.getContextName());
        }).findFirst();
        if(optional.isPresent()){
            return optional.get();
        }
        return  null;
    }

}
