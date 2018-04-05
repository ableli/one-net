package com.weyong.onenet.client.serverSession;

import com.weyong.onenet.client.OneNetClient;
import com.weyong.onenet.client.config.ServerConfig;
import com.weyong.onenet.dto.DataTransfer;
import com.weyong.onenet.client.OneNetChannelInitializer;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
public class OneNetServerSessionManager {
    private ConcurrentHashMap<String, ServerSession> oneNetServerSessions = new ConcurrentHashMap<>();

    @Autowired
    OneNetClient oneNetClient;

    @Scheduled(fixedRate = 2000)
    private void heartbeat(){
        oneNetServerSessions.values().stream().forEach(serverSession -> {
            if(serverSession.getLastHeartbeatTime()==null){
                newOneChannel(serverSession);
                return;
            }
            Calendar lastHeartBeatTimeCondition = Calendar.getInstance();
            lastHeartBeatTimeCondition.add(Calendar.SECOND,-5);
            if(lastHeartBeatTimeCondition.getTime().after(serverSession.getLastHeartbeatTime())) {
                log.info(String.format("Server Session %s inactive.Try to renew.", serverSession.getServerConfig().getServerName()));
                newOneChannel(serverSession);
            }
            heatrbeatOneChannel(serverSession);
        });
    }

    private void heatrbeatOneChannel(ServerSession serverSession) {
        try {
            DataTransfer dt = new DataTransfer();
            dt.setOpType(DataTransfer.OP_TYPE_HEART_BEAT);
            serverSession.getServerChannel().writeAndFlush(dt);
            log.debug(String.format("Heartbeat server session %s",serverSession.getServerConfig().getServerName()));
        }catch (Exception ex) {
            log.error(String.format("Heartbeat server session %s meet ex , and  it is :%s",
                    serverSession.getServerConfig().getServerName(),
                    ex.getMessage()));
        }
    }

    private void newOneChannel(ServerSession serverSession) {
        try {
            log.info(String.format("Start to connect server session %s",serverSession.getServerConfig().getServerName()));
            ServerConfig serverConfig = serverSession.getServerConfig();
            Channel socketChannel = OneNetClient.createChannel(serverConfig.getHostName(), serverConfig.getOneNetPort(), new OneNetChannelInitializer(serverSession));
            DataTransfer dt = new DataTransfer();
            dt.setOpType(DataTransfer.OP_TYPE_NEW);
            socketChannel.writeAndFlush(dt);
            serverSession.setServerChannel(socketChannel);
            log.info(String.format("Server session %s established",serverSession.getServerConfig().getServerName()));
        }catch (Exception ex){
            log.error(String.format("Connection server session %s meet ex , and  it is :%s",
                    serverSession.getServerConfig().getServerName(),
                    ex.getMessage()));
        }
    }
}
