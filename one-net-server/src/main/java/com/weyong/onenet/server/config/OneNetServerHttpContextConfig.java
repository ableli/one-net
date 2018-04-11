package com.weyong.onenet.server.config;

import lombok.Data;

import java.util.List;

/**
 * Created by hao.li on 4/10/2018.
 */
@Data
public class OneNetServerHttpContextConfig {
    private List<String> domainRegExs;
    private int getInternetPort(){
        return 80;
    }
}
