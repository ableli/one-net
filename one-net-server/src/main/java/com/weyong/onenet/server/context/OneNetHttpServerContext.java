package com.weyong.onenet.server.context;

import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.config.OneNetServerContextConfig;
import com.weyong.onenet.server.config.OneNetServerHttpContextConfig;

/**
 * Created by hao.li on 4/10/2018.
 */
public class OneNetHttpServerContext extends OneNetServerContext {
    public OneNetHttpServerContext(OneNetServerHttpContextConfig oneNetServerContextConfig, OneNetServer oneNetServer) {
        super((OneNetServerContextConfig)oneNetServerContextConfig, oneNetServer);
    }
}
