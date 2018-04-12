package com.weyong.onenet.server.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Created by hao.li on 4/10/2018.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class OneNetServerHttpContextConfig extends OneNetServerContextConfig {
    private List<String> domainRegExs;

    @Override
    public Integer getInternetPort(){
        return 80;
    }
}
