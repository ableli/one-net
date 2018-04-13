package com.weyong.onenet.server.manager;

import com.weyong.onenet.server.session.ClientSession;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by haoli on 2018/4/12.
 */
public class OneNetHttpConnectionManager extends OneNetConnectionManager {


    @Override
    public Channel getAvailableChannel(String hostName) {
        Channel channel = null;
        Optional<Map.Entry<String, List<ClientSession>>> target = this.getContextNameSessionMap().entrySet().stream().filter((entry) -> {
            return Pattern.compile(entry.getKey()).matcher(hostName).matches();
        }).findFirst();
        if (target.isPresent()) {
            return getChannel(target.get().getValue());
        }
        return null;
    }

}
