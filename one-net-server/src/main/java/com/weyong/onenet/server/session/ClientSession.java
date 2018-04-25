package com.weyong.onenet.server.session;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by haoli on 2018/4/6.
 */
@Data
@AllArgsConstructor
public class ClientSession {
    private String clientName;
    private Channel clientChannel;
    private Date lastHeartBeatTime;

    public ClientSession(String clientName) {
        this.clientName = clientName;
    }

    public boolean isActive() {
        Calendar ruleTime = Calendar.getInstance();
        ruleTime.add(Calendar.SECOND,-5);
        return lastHeartBeatTime.compareTo(ruleTime.getTime()) > 0;
    }

    public void close() {
        if(clientChannel!=null) {
            clientChannel.close();
        }
    }
}
