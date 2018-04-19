package com.weyong.onenet.client.config;

import lombok.Data;

/**
 * Created by haoli on 4/19/2018.
 */
@Data
public class PoolConfig {
    private int maxIdle = 8;
    private int maxTotal = 10240;
    private int minIdle =8;
    private boolean blockWhenExhausted =true;
    private boolean testWhileIdle = false;
    private boolean testOnBorrow = true;
    private boolean testOnReturn = false;
    private int maxWaitMillis = 1000;
    private int TimeBetweenEvictionRunsMillis = 30000;
    private String evictionPolicyClassName="com.weyong.onenet.client.context.OneNetPoolEvictionPolicy";
}
