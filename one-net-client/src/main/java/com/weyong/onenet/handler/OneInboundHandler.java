package com.weyong.onenet.handler;

import com.weyong.onenet.OneChannelRenewTask;
import com.weyong.onenet.OnenetClient;
import com.weyong.onenet.dto.DataTransfer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
public class OneInboundHandler extends SimpleChannelInboundHandler<DataTransfer> {
    public static final ConcurrentHashMap<String,Channel> localSocketMap = new
            ConcurrentHashMap<String,Channel>();

    private static Executor executor = Executors.newCachedThreadPool();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataTransfer msg) {
        if(DataTransfer.OP_TYPE_HEART_BEAT.equals(msg.getOpType())){
            OneChannelRenewTask.lastHeartBeatTime = new Date();
        }else {
            String key = msg.getSessionId();
            if (StringUtils.isNotEmpty(key)) {
                Channel localChannel = localSocketMap.computeIfAbsent(key, k -> OnenetClient.getPooledLocalChannel(key));
                if (!StringUtils.isEmpty(msg.getOpType())) {
                    if (DataTransfer.OP_TYPE_CLOSE.equals(msg.getOpType())) {
                        localSocketMap.remove(key);
                        OnenetClient.returnChannel(localChannel);
                    }
                } else {
                    localChannel.writeAndFlush(msg.getData());
                }
            }
        }
    }
}
