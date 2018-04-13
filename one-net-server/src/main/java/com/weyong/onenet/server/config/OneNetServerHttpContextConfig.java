package com.weyong.onenet.server.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by hao.li on 4/10/2018.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OneNetServerHttpContextConfig extends OneNetServerContextConfig {
    private List<String> domainRegExs;

    @Override
    public Integer getInternetPort() {
        return 80;
    }

    @Override
    public String toString() {
        return String.format("Context: %s, domains: [%s]", this.getContextName(), StringUtils.join(domainRegExs, ","));
    }
}
