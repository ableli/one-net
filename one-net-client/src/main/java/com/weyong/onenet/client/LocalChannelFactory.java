package com.weyong.onenet.client;

import com.weyong.onenet.OnenetClient;
import com.weyong.onenet.handler.LocalInboudHandler;
import com.weyong.onenet.initializer.LocalChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by haoli on 2017/4/17.
 */
@Slf4j
public class LocalChannelFactory  extends BasePooledObjectFactory<Channel> {
    @Override
    public Channel create() throws Exception {
       return OnenetClient.getLocalChannel(StringUtils.EMPTY);
    }

    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        return new DefaultPooledObject<Channel>(obj);
    }

    @Override
    public boolean validateObject(PooledObject<Channel> p) {
        if(!p.getObject().isActive()){
            log.debug(String.format("Local target channel is inactive. exist time:%d",System.currentTimeMillis()-p.getCreateTime()));
            return false;
        }
        return true;
    }

    @Override
    public void passivateObject(PooledObject<Channel> pooledObject) {
        ChannelHandler handler =  pooledObject.getObject().pipeline().get(LocalChannelInitializer.LOCAL_RESPONSE_HANDLER);
        ((LocalInboudHandler)handler).setOutsideChannelId(StringUtils.EMPTY);
    }
}
