package com.weyong.onenet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

/**
 * Created by haoli on 2018/4/7.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HeartbeatPackage extends BasePackage {
    private static HeartbeatPackage heartbeatPackage = new HeartbeatPackage();

    public HeartbeatPackage() {
        super(DataPackage.HEART_BEAT);
    }

    public static BasePackage instance() {
        return heartbeatPackage;
    }

    @Override
    protected void fillBody(ByteBuffer byteBuffer) {

    }
}
