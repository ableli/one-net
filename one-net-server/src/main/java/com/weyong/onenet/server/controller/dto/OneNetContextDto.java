package com.weyong.onenet.server.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by haoli on 2018/4/14.
 */
@Data
@AllArgsConstructor
public class OneNetContextDto {
    private String name;
    private Integer liveSessionCount;
}
