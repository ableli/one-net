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
public class InvalidSessionPackage extends BasePackage {
    public InvalidSessionPackage(String contextName, Long sessionId) {
        super(BasePackage.INVALID_SESSION);
        this.setContextName(contextName);
        this.setSessionId(sessionId);
    }

    public InvalidSessionPackage(ByteBuf byteBuf) {
        super(BasePackage.INVALID_SESSION);
        this.setContextName(stringDecoding(byteBuf));
        this.setSessionId(byteBuf.readLong());
    }

    @Override
    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(getContextName(), byteBuffer);
        byteBuffer.putLong(getSessionId());
    }
}
