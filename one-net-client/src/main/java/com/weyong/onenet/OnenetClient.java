package com.weyong.onenet;

import com.weyong.onenet.client.LocalChannelFactory;
import com.weyong.onenet.dto.DataTransfer;
import com.weyong.onenet.handler.LocalInboudHandler;
import com.weyong.onenet.initializer.LocalChannelInitializer;
import com.weyong.onenet.initializer.OneChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.*;

/**
 * Created by hao.li on 2017/4/13.
 */
@Slf4j
public class OnenetClient {
    private static Bootstrap b;
    private static String outsideIp="localhost";
    private static int outsidePort = 81;
    private static int localPort = 8008;
    private static String targetHost = "127.0.0.1";
    protected static Channel oneChannel;
    private static ObjectPool<Channel> localPool;
    protected static boolean instantConnection = false;

    public static void main(String[] args) throws Exception {
        if(args==null || args.length < 3){
            System.out.println("append args: oneChannelHost oneChannelPort localPort [targetHost,defalt localhost]");
            return;
        }
        outsideIp = args[0];
        outsidePort = Integer.parseInt(args[1]);
        localPort = Integer.parseInt(args[2]);
        if(args.length>3) {
            instantConnection = Integer.parseInt(args[3]) > 0;
            if(args.length>4){
                targetHost= args[4];
            }
        }
        b = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);
        oneChannel();
        if(!instantConnection) {
            localPool = new GenericObjectPool<Channel>(new LocalChannelFactory(),
                    getGenericObjectPoolConfig());
        }
    }

    public static void oneChannel() {
        new Thread( new OneChannelRenewTask(outsideIp,outsidePort)).start();
    }

    public static GenericObjectPoolConfig getGenericObjectPoolConfig() {
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

    public static Channel getChannel(final String ip, final int port,ChannelInitializer channelInitializer) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = b.handler(channelInitializer).connect(ip,port).sync();
            return channelFuture.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Channel getLocalChannel(String outsideChannelId) {
        return getChannel(targetHost,localPort,new LocalChannelInitializer(outsideChannelId));
    }

    public static Channel getPooledLocalChannel(String outsideChannelId){
        Channel channel = null;
        if(instantConnection){
            channel = getLocalChannel(outsideChannelId);
        }else{
            try {
                channel =  localPool.borrowObject();
            } catch (Exception e) {
               throw new RuntimeException("Can't borrow object from pool. - "+e.getMessage());
            }
            ChannelHandler handler = channel.pipeline().get(LocalChannelInitializer.LOCAL_RESPONSE_HANDLER);
            ((LocalInboudHandler)handler).setOutsideChannelId(outsideChannelId);
            log.debug(localPool.getNumActive()+"-"+localPool.getNumIdle());
        }
       return channel;
    }

    public static void returnChannel(Channel channel) {
        if(instantConnection){
            channel.close();
        }else {
            log.debug("Before return :" + localPool.getNumActive() + "-" + localPool.getNumIdle());
            try {
                localPool.returnObject(channel);
            } catch (Exception e) {
               log.info(e.getMessage()+localPool.getNumActive() + "-" + localPool.getNumIdle());
            }
            log.debug("After return :" + localPool.getNumActive() + "-" + localPool.getNumIdle());
        }
    }

    public static void removeFromPool(Channel channel) {
        if(instantConnection){
        }else {
            try {
                localPool.invalidateObject(channel);
            } catch (Exception e) {
                log.info(e.getMessage()+localPool.getNumActive() + "-" + localPool.getNumIdle());
            }
        }
    }

    public static Channel getOneChannel(){
        return oneChannel;
    }
}
