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
public class SessionInvalidPackage extends BasePackage {
    public SessionInvalidPackage(String contextName, Long sessionId) {
        super(BasePackage.OP_TYPE_CLOSE);
        this.setContextName( contextName);
        this.setSessionId(sessionId);
    }

    public SessionInvalidPackage(ByteBuf byteBuf) {
        super(BasePackage.OP_TYPE_CLOSE);
        this.setContextName(stringDecoding(byteBuf));
        this.setSessionId(byteBuf.readLong());
    }

    @Override
    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(getContextName(),byteBuffer);
        byteBuffer.putLong(getSessionId());
    }
}
