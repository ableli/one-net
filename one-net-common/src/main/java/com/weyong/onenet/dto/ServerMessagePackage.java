package com.weyong.onenet.dto;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

/**
 * Created by haoli on 2018/4/7.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ServerMessagePackage extends BasePackage {
    private String msg;
    public ServerMessagePackage(String msg) {
        super(DataPackage.OP_TYPE_ERROR);
        this.msg = msg;
    }

    public ServerMessagePackage(ByteBuf byteBuf) {
        super(DataPackage.OP_TYPE_ERROR);
        this.setMsg(stringDecoding(byteBuf));
    }

    @Override
    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(msg,byteBuffer);
    }
}
