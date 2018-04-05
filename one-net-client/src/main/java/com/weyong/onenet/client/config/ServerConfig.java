package com.weyong.onenet.client.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by haoli on 2018/4/6.
 */
@Slf4j
@Data
public class ServerConfig {
    private String serverName;
    private String hostName;
    private Integer oneNetPort;
    private List<OneNetClientContextConfig> oneNetClientContextConfigs;
}
