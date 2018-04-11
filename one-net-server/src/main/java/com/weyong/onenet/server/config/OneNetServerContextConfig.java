package com.weyong.onenet.server.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
public class OneNetServerContextConfig {
    private String contextName;
    private boolean zip;
    private boolean aes;
    private Integer kBps;
    private Integer internetPort;
}
