package com.weyong.onenet.server;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * Created by haoli on 2018/4/6.
 */
@Data
public class ClientSession {
    private String clientName;
    private Channel clientChannel;

    public ClientSession(String clientName) {
        this.clientName = clientName;
    }
}
