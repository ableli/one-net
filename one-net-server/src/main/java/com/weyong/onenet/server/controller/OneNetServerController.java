package com.weyong.onenet.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.weyong.onenet.server.OneNetServer;
import com.weyong.onenet.server.controller.dto.OneNetContextDto;
import com.weyong.onenet.server.controller.dto.OneNetServerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

/**
 * Created by haoli on 2018/4/14.
 */
@Slf4j
@Service
@RequestMapping(value = "/onenet")
public class OneNetServerController {

    @Autowired
    private OneNetServer oneNetServer;

    @RequestMapping(value = "/server", method = RequestMethod.GET)
    @ResponseBody
    public String server(){
        OneNetServerDto serverDto = new OneNetServerDto();
        serverDto.setName(oneNetServer.getOneNetServerConfig().getName());
        serverDto.setPort(oneNetServer.getOneNetServerConfig().getOneNetPort());
        serverDto.setContexts(new ArrayList<>());
        if(!CollectionUtils.isEmpty(this.oneNetServer.getContexts())) {
            this.oneNetServer.getContexts().values().stream().forEach(oneNetServerContext -> {
                serverDto.getContexts().add(new OneNetContextDto(oneNetServerContext.getOneNetServerContextConfig().getContextName(),
                        oneNetServerContext.getOneNetSessions().size()));

            });
        }
        return JSON.toJSONString(serverDto, SerializerFeature.PrettyFormat);
    }
}
