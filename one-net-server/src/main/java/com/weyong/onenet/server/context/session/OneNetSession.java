package com.weyong.onenet.server.context.session;

import com.weyong.onenet.dto.DataTransfer;
import com.weyong.onenet.server.context.OneNetServerContext;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Created by haoli on 2018/4/5.
 */
@Data
@Slf4j
public class OneNetSession {
    private static Random random = new Random(1);
    private Long sessionId;
    private OneNetServerContext oneNetServerContext;
    private Channel internetChannel;
    private Channel oneNetChannel;


    public OneNetSession(OneNetServerContext oneNetServerContext, SocketChannel ch, Channel oneNetChannel) {
        this.sessionId = random.nextLong();
        this.internetChannel = ch;
        this.oneNetChannel  = oneNetChannel;
    }

    public void close() {
       this.close(false);
    }

    public void close(boolean fromOneNetClient) {
        if(!fromOneNetClient) {
            oneNetChannel.writeAndFlush(new DataTransfer(sessionId, DataTransfer.OP_TYPE_CLOSE));
        }else{
            if(internetChannel!=null&& internetChannel.isActive()){
                internetChannel.close();
            }
        }
        oneNetServerContext.removeSession(sessionId);
    }
}
