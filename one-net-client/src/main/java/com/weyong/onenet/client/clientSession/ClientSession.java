package com.weyong.onenet.client.clientSession;

import com.weyong.onenet.client.OneNetClientContext;
import com.weyong.onenet.client.OneNetInboundHandler;
import com.weyong.onenet.dto.DataTransfer;
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
    private Channel oneNetChannel;
    private OneNetClientContext oneNetClientContext;
    private Channel localChannel;

    public void close() {
        oneNetClientContext.getSessionMap().remove(sessionId);
        oneNetChannel.writeAndFlush(new DataTransfer(sessionId,DataTransfer.OP_TYPE_CLOSE));
        oneNetClientContext.removeFromPool(localChannel);
    }
}
