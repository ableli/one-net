package com.weyong.onenet.server.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by haoli on 2018/4/5.
 */
@Data
@ConfigurationProperties("oneNetServer")
public class OneNetServerConfig {
    private String name;
    private Integer oneNetPort;
    private List<OneNetServerContextConfig> contexts;
}
