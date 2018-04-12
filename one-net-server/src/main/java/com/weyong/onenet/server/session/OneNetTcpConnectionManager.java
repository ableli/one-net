package com.weyong.onenet.server.session;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by haoli on 2018/4/12.
 */
@Slf4j
public class OneNetTcpConnectionManager extends OneNetConnectionManager {
    public Channel getAvailableChannel(String contextName) {
        if(!this.getContextNameSessionMap().containsKey(contextName)){
            return null;
        }
        List<ClientSession> sessions = getContextNameSessionMap().get(contextName);
        return getChannel(sessions);
    }



    public ClientSession computeClientSession(String clientName, List<String> contextNames, Channel channel) {
        ClientSession clientSession = new ClientSession(clientName,channel);
        contextNames.stream().forEach((contextName)->{
            List<ClientSession> sessions = getContextNameSessionMap().computeIfAbsent(contextName,(name)->new LinkedList<ClientSession>());
            log.info(String.format("Client context %s -> %s added.",clientName ,contextName));
            sessions.add(clientSession);
        });
        return clientSession;
    }
}
