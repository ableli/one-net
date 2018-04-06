package com.weyong.onenet.client.session;

import com.weyong.onenet.client.config.OnenetClientServerConfig;
import com.weyong.onenet.client.context.OneNetClientContext;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by haoli on 2018/4/6.
 */
@Slf4j
@Data
public class ServerSession {
    private Channel serverChannel;
    private Date lastHeartbeatTime;
    private OnenetClientServerConfig onenetClientServerConfig;
    private Map<String,OneNetClientContext> oneNetClientContextMap = new HashMap<>();

    public ServerSession(OnenetClientServerConfig onenetClientServerConfig) {
        this.onenetClientServerConfig = onenetClientServerConfig;
        onenetClientServerConfig.getContexts().forEach(oneNetClientContextConfig -> {
            oneNetClientContextMap.putIfAbsent(oneNetClientContextConfig.getContextName(),
                    new OneNetClientContext(oneNetClientContextConfig));
        });
    }
}
