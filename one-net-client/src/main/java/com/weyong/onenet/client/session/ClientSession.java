package com.weyong.onenet.client.session;

import com.weyong.onenet.dto.InvalidSessionPackage;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by haoli on 2018/4/6.
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSession {
    private Long sessionId;
    private ServerSession serverSession;
    private String contextName;
    private Channel localChannel;

    public void close() {
        if(this.getServerSession().getServerChannel().isActive()) {
            serverSession.getServerChannel().writeAndFlush(
                    new InvalidSessionPackage(this.getContextName(), sessionId));
        }
    }
}
