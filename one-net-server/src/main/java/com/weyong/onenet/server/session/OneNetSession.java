package com.weyong.onenet.server.session;

import com.weyong.onenet.dto.InvalidSessionPackage;
import io.netty.channel.Channel;
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
    private ClientSession clientSession;


    public OneNetSession(String contextName, Channel ch, ClientSession clientSession) {
        this.sessionId = index.incrementAndGet();
        this.internetChannel = ch;
        this.clientSession = clientSession;
        this.contextName = contextName;
    }

    public void close() {
        if (this.clientSession.isActive()) {
            clientSession.getClientChannel().writeAndFlush(new InvalidSessionPackage(getContextName(), sessionId));
        }
        if (internetChannel != null && internetChannel.isActive()) {
            internetChannel.close();
        }
    }

    @Override
    public String toString(){
        return String.format("Session:%d Context:%s OutChannel:%s ClientSession:%s OneNetChannel:%s",sessionId,contextName,
                internetChannel.id().asShortText(),clientSession.getClientName(),clientSession.getClientChannel().id().asShortText());
    }
}
