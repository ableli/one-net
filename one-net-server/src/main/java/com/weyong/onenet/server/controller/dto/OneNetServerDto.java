package com.weyong.onenet.server.controller.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by haoli on 2018/4/14.
 */
@Data
public class OneNetServerDto {
    private String name;
    private Integer port;
    private List<OneNetContextDto> contexts;
    private List<OneNetClientSessionDto> clientSessions;
}
