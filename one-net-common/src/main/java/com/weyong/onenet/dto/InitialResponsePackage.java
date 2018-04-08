package com.weyong.onenet.dto;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by haoli on 2018/4/7.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class InitialResponsePackage extends BasePackage {
    private boolean zip;
    private boolean aes;
    private int kBps;
    public InitialResponsePackage(String contextName,boolean zip, boolean aes, int kBps){
        super(BasePackage.INITIAL_RESPONSE);
        this.setContextName(contextName);
        this.zip = zip;
        this.aes = aes;
        this.kBps = kBps;
    }

    public InitialResponsePackage(ByteBuf byteBuf) {
        super(BasePackage.INITIAL_RESPONSE);
        this.setContextName(stringDecoding(byteBuf));
        int boolValues = (int)byteBuf.readByte();
        aes = boolValues%2 == 1;
        boolValues = boolValues >> 1;
        zip = boolValues%2 == 1;
        kBps = byteBuf.readInt();
    }

    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(this.getContextName(),byteBuffer);
        byteBuffer.put((byte)this.getBoolValues(zip,aes));
        byteBuffer.putInt(kBps);
    }
}
