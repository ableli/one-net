package com.weyong.onenet.client.context;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.EvictionConfig;


/**
 * Created by haoli on 4/19/2018.
 */
@Slf4j
public class OneNetPoolEvictionPolicy extends DefaultEvictionPolicy<Channel> {
    @Override
    public boolean evict(EvictionConfig config, PooledObject<Channel> underTest,
                         int idleCount) {

       if(super.evict(config,underTest,idleCount)){
           log.debug("Pool object eviction check failed.");
           return true;
       }
       if(!underTest.getObject().isActive()){
           log.debug("Pool object eviction check failed.");
           return true;
       }
       log.debug("Pool object eviction check pass.");
       return false;
    }
}
