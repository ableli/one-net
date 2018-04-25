package com.weyong.onenet.server.manager;

import com.weyong.onenet.server.session.ClientSession;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
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
    private ConcurrentHashMap<String, HashMap<String,ClientSession>> contextNameSessionMap = new ConcurrentHashMap<>();

    public abstract ClientSession getAvailableSession(String contextName);

    protected ClientSession getSession(List<ClientSession> sessions) {
        if (CollectionUtils.isEmpty(sessions)) {
            return null;
        }
        ClientSession selectedSession = null;
        while (selectedSession == null && !CollectionUtils.isEmpty(sessions)) {
            int selectedOne = getRandom().incrementAndGet() % sessions.size();
            ClientSession clientSession = sessions.get(selectedOne);
            if (clientSession != null && clientSession.isActive()) {
                selectedSession =clientSession;
            } else {
                sessions.remove(clientSession);
            }
        }
        return selectedSession;
    }

    public void registerClientSession(String contextName,String clientName, ClientSession clientSession) {
        HashMap<String ,ClientSession> clientSessionMap = this.getContextNameSessionMap().getOrDefault(contextName, new HashMap<>());
        if (clientSessionMap.containsKey(clientName)) {
            ClientSession oldSession =  clientSessionMap.replace(clientName, clientSession);
            oldSession.close();
        }else{
            clientSessionMap.putIfAbsent(clientName,clientSession);
        }
        this.getContextNameSessionMap().putIfAbsent(contextName,clientSessionMap);
    }

}
