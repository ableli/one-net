package com.weyong.onenet.server.session;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
public class OneNetConnectionManager {
    private AtomicInteger random = new AtomicInteger(1);
    private ConcurrentHashMap<String, List<ClientSession>> contextNameSessionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ClientSession> oneNetClientSessionMap = new ConcurrentHashMap<>();

    public Channel getAvailableChannel(String contextName) {
        if(!contextNameSessionMap.containsKey(contextName)){
            return null;
        }
        List<ClientSession>  sessions = contextNameSessionMap.get(contextName);
        if(CollectionUtils.isEmpty(sessions)){
            return null;
        }
        Channel selectedChannel = null;
        while(selectedChannel == null && !CollectionUtils.isEmpty(sessions)){
            int selectedOne = random.incrementAndGet()%sessions.size();
            ClientSession clientSession =  sessions.get(selectedOne);
            if(clientSession!=null && clientSession.getClientChannel()!=null){
                selectedChannel = clientSession .getClientChannel();
            }else{
                sessions.remove(clientSession);
            }
        }
        return selectedChannel;
    }

    public void refreshSessionChannel(String clientName, List<String> contextNames, Channel channel) {
       ClientSession clientSession = oneNetClientSessionMap.computeIfAbsent(clientName,(name)-> new ClientSession(name) );
        if(clientSession.getClientChannel() != null){
            log.info(String.format("Client session %s renew",clientSession.getClientName()));
            clientSession.getClientChannel().close();
        }
        clientSession.setClientChannel(channel);
        contextNames.stream().forEach((contextName)->{
            List<ClientSession> sessions = contextNameSessionMap.computeIfAbsent(contextName,(name)->new LinkedList<ClientSession>());
            if(!sessions.contains(clientSession)){
                log.info(String.format("Client context %s -> %s added.",clientName ,contextName));
                sessions.add(clientSession);
            }
        });
    }
}
