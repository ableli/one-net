package com.weyong.onenet.server;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by haoli on 2018/4/5.
 */
@Slf4j
@Data
public class OneNetConnectionManager {
    private ConcurrentHashMap<String, Channel> oneNetChannels = new ConcurrentHashMap<>();
}
