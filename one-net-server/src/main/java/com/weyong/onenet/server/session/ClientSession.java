package com.weyong.onenet.server.session;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by haoli on 2018/4/6.
 */
@Data
@AllArgsConstructor
public class ClientSession {
    private String clientName;
    private Channel clientChannel;

    public ClientSession(String clientName) {
        this.clientName = clientName;
    }

    public boolean isActive() {
        return clientChannel != null && clientChannel.isOpen();
    }
}
