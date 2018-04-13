package com.weyong.onenet.server.session;

import com.weyong.onenet.dto.InvalidSessionPackage;
import com.weyong.onenet.server.context.OneNetServerContext;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by haoli on 2018/4/5.
 */
@Data
@Slf4j
public class OneNetSession {
    private static AtomicLong index = new AtomicLong(1);
    private Long sessionId;
    private String contextName;
    private Channel internetChannel;
    private Channel oneNetChannel;


    public OneNetSession(String contextName, SocketChannel ch, Channel oneNetChannel) {
        this.sessionId = index.incrementAndGet();
        this.internetChannel = ch;
        this.oneNetChannel = oneNetChannel;
        this.contextName = contextName;
    }

    public void close() {
        if(this.oneNetChannel.isActive()) {
            oneNetChannel.writeAndFlush(new InvalidSessionPackage(getContextName(), sessionId));
        }
        if (internetChannel != null && internetChannel.isActive()) {
            internetChannel.close();
        }
    }
}
