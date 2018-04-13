package com.weyong.onenet.client.session;

import com.weyong.onenet.client.context.OneNetClientContext;
import com.weyong.onenet.dto.InvalidSessionPackage;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by haoli on 2018/4/6.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSession {
    private Long sessionId;
    private ServerSession serverSession;
    private OneNetClientContext oneNetClientContext;
    private Channel localChannel;

    public void closeFromLocal() {
        oneNetClientContext.getSessionMap().computeIfPresent(sessionId, (sessionId, clientSession) -> {
            serverSession.getServerChannel().writeAndFlush(
                    new InvalidSessionPackage(clientSession.getContextName(), sessionId));
            oneNetClientContext.removeFromPool(localChannel);
            return null;
        });
    }

    public String getContextName() {
        return oneNetClientContext.getOneNetClientContextConfig().getContextName();
    }

    public void closeFromOneNet() {
        oneNetClientContext.getSessionMap().remove(sessionId);
        localChannel.close();
        oneNetClientContext.removeFromPool(localChannel);
    }
}
