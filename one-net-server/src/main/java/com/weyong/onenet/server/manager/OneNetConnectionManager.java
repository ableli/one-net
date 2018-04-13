package com.weyong.onenet.server.manager;

import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.session.ClientSession;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
public abstract class OneNetConnectionManager {
    private AtomicInteger random = new AtomicInteger(1);
    private ConcurrentHashMap<String, List<ClientSession>> contextNameSessionMap = new ConcurrentHashMap<>();

    public abstract Channel getAvailableChannel(String contextName);

    protected Channel getChannel(List<ClientSession> sessions) {
        if(CollectionUtils.isEmpty(sessions)){
            return null;
        }
        Channel selectedChannel = null;
        while(selectedChannel == null && !CollectionUtils.isEmpty(sessions)){
            int selectedOne = getRandom().incrementAndGet()%sessions.size();
            ClientSession clientSession =  sessions.get(selectedOne);
            if(clientSession!=null && clientSession.getClientChannel()!=null){
                selectedChannel = clientSession .getClientChannel();
            }else{
                sessions.remove(clientSession);
            }
        }
        return selectedChannel;
    }

    public void registerClientSession(String name, ClientSession clientSession) {
        this.getContextNameSessionMap().compute(name,(key,sessions)->{
            if(sessions == null){
                sessions = new LinkedList<>();
            }
            if(!sessions.contains(clientSession)){
                sessions.add(clientSession);
            }
            return sessions;
        });
    }

}
