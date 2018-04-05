package com.weyong.onenet.server;

import com.weyong.onenet.server.context.OneNetServerContextConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by haoli on 2018/4/5.
 */
@Data
@Slf4j
public class OneNetServerConfig {
    private String name;
    private Integer oneNetPort;
    private String oneNetKey;
    private List<OneNetServerContextConfig> oneNetServerContextConfigs;
}
