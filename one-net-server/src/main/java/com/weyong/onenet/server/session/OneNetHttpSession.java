package com.weyong.onenet.server.session;

import com.weyong.onenet.dto.DataPackage;
import com.weyong.onenet.server.config.OneNetServerHttpContextConfig;
import com.weyong.onenet.server.context.OneNetServerContext;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by haoli on 2018/4/12.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OneNetHttpSession extends OneNetSession {
    ConcurrentLinkedDeque<DataPackage> queue = new ConcurrentLinkedDeque<>();
    private OneNetServerHttpContextConfig contextConfig;

    public OneNetHttpSession(OneNetServerContext oneNetServerContext, SocketChannel ch, Channel channel) {
        super(oneNetServerContext, ch, channel);
    }

    @Override
    public String getContextName() {
        return contextConfig.getContextName();
    }
}
