package com.weyong.onenet.dto;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

/**
 * Created by haoli on 2018/4/7.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MessagePackage extends BasePackage {
    private String msg;

    public MessagePackage(String msg) {
        super(DataPackage.MESSAGE);
        this.msg = msg;
    }

    public MessagePackage(ByteBuf byteBuf) {
        super(DataPackage.MESSAGE);
        this.setMsg(stringDecoding(byteBuf));
    }

    @Override
    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(msg, byteBuffer);
    }
}
