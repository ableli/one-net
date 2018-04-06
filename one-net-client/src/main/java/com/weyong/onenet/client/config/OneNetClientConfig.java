package com.weyong.onenet.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by haoli on 2018/4/6.
 */

@Data
@ConfigurationProperties("oneNetClient")
public class OneNetClientConfig {
    private String serverName;
    private List<OnenetClientServerConfig> serverConfigs;
}
