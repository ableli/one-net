package com.weyong.onenet.client.context;

import com.weyong.onenet.client.OneNetClient;
import com.weyong.onenet.client.config.OneNetClientContextConfig;
import com.weyong.onenet.client.handler.LocalChannelFactory;
import com.weyong.onenet.client.initializer.LocalChannelInitializer;
import com.weyong.onenet.client.handler.LocalInboudHandler;
import com.weyong.onenet.client.session.ClientSession;
import com.weyong.onenet.client.session.ServerSession;
import com.weyong.onenet.dto.InvalidSessionPackage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by haoli on 2018/4/6.
 */
@Data
@Slf4j
public class OneNetClientContext {
    private ObjectPool<Channel> localPool;
    private OneNetClientContextConfig oneNetClientContextConfig;
    private Map<Long, ClientSession> sessionMap = new HashMap<>();
    private boolean zip;
    private boolean aes;
    private int kBps;

    public OneNetClientContext(OneNetClientContextConfig oneNetClientContextConfig) {
        this.oneNetClientContextConfig = oneNetClientContextConfig;
        if (oneNetClientContextConfig.isLocalPool()) {
            localPool = new GenericObjectPool<Channel>(new LocalChannelFactory(
                    () -> OneNetClient.createChannel(oneNetClientContextConfig.getLocalhost(), oneNetClientContextConfig.getPort(), new LocalChannelInitializer(this,null))
            ),
                    getGenericObjectPoolConfig());
        }
    }

    private GenericObjectPoolConfig getGenericObjectPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(50);
        poolConfig.setMaxTotal(1024);
        poolConfig.setMinIdle(25);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setFairness(true);
        poolConfig.setMaxWaitMillis(1000);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTimeBetweenEvictionRunsMillis(1000);
        return poolConfig;
    }

    public Channel getContextLocalChannel(ClientSession clientSession) {
        Channel channel = null;
        if (!oneNetClientContextConfig.isLocalPool()) {
            channel = OneNetClient.createChannel(oneNetClientContextConfig.getLocalhost(),
                    oneNetClientContextConfig.getPort(),
                    new LocalChannelInitializer(this,clientSession));
        } else {
            try {
                channel = localPool.borrowObject();
            } catch (Exception e) {
                throw new RuntimeException("Can't borrow object from pool. - " + e.getMessage());
            }
            ChannelHandler handler = channel.pipeline().get(LocalChannelInitializer.LOCAL_RESPONSE_HANDLER);
            ((LocalInboudHandler) handler).setClientSession(clientSession);
            ChannelTrafficShapingHandler channelTrafficShapingHandler =
                    (ChannelTrafficShapingHandler) channel.pipeline().get(LocalChannelInitializer.CHANNEL_TRAFFIC_HANDLER);
            channelTrafficShapingHandler.setReadLimit(kBps * 1024);
            channelTrafficShapingHandler.setWriteLimit(kBps * 1024);
            log.debug(localPool.getNumActive() + "-" + localPool.getNumIdle());
        }
        return channel;
    }

    public void returnChannel(Channel channel) {
        if (!oneNetClientContextConfig.isLocalPool()) {
            channel.close();
        } else {
            log.debug("Before return :" + localPool.getNumActive() + "-" + localPool.getNumIdle());
            try {
                localPool.returnObject(channel);
            } catch (Exception e) {
                log.info(e.getMessage() + localPool.getNumActive() + "-" + localPool.getNumIdle());
            }
            log.debug("After return :" + localPool.getNumActive() + "-" + localPool.getNumIdle());
        }
    }

    public void removeFromPool(Channel channel) {
        if (oneNetClientContextConfig.isLocalPool()) {
            try {
                localPool.invalidateObject(channel);
            } catch (Exception e) {
                log.info(e.getMessage() + localPool.getNumActive() + "-" + localPool.getNumIdle());
            }
        }
    }

    public void close(ClientSession clientSession) {
        close(clientSession.getSessionId());
    }

    public void closeAll() {
        List<ClientSession> clientSessionList = this.getSessionMap().values().stream().collect(Collectors.toList());
        clientSessionList.forEach((targetSession)-> close(targetSession));
    }

    public void close(Long sessionId) {
        ClientSession targetSession = this.getSessionMap().remove(sessionId);
        if(targetSession!=null) {
            targetSession.close();
            returnChannel(targetSession.getLocalChannel());
        }
    }

    public ClientSession getCurrentSession(ServerSession serverSession, Long sessionId) {
        ClientSession targetSession =  new ClientSession(sessionId, serverSession, this.getOneNetClientContextConfig().getContextName(), null);
            try {
                targetSession.setLocalChannel(this.getContextLocalChannel(targetSession));
                this.getSessionMap().putIfAbsent(sessionId,targetSession);
                return targetSession;
            } catch (Exception ex) {
                log.error(ex.getMessage());
                return null;
            }
    }
}
