package com.weyong.onenet.server.manager;

import com.weyong.onenet.server.session.ClientSession;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by haoli on 2018/4/12.
 */
@Slf4j
public class OneNetTcpConnectionManager extends OneNetConnectionManager {
    @Override
    public ClientSession getAvailableSession(String contextName) {
        if (!this.getContextNameSessionMap().containsKey(contextName)) {
            return null;
        }
        Map sessionsMap = getContextNameSessionMap().get(contextName);
        return getSession(new ArrayList<>(sessionsMap.values()));
    }
}
