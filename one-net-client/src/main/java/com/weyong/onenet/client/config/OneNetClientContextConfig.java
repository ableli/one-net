package com.weyong.onenet.client.config;

import lombok.Data;

/**
 * Created by haoli on 2018/4/6.
 */
@Data
public class OneNetClientContextConfig {
    private String contextName;
    private String localhost;
    private Integer port;
    private boolean localPool;
    private PoolConfig poolConfig;
}
