package com.weyong.onenet.dto;

import io.netty.buffer.ByteBuf;
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
public class InitialPackage extends BasePackage {
    private String clientName;
    private List<String> contextNames;
    private String serverKey;
    public InitialPackage(){
        super(BasePackage.INITIAL);
    }

    public InitialPackage(ByteBuf byteBuf) {
        super(BasePackage.INITIAL);
        this.setClientName(stringDecoding(byteBuf));
        this.setServerKey(stringDecoding(byteBuf));
        this.setContextNames(new ArrayList<>());
        while(byteBuf.readableBytes()>0){
            this.getContextNames().add(stringDecoding(byteBuf));
        }
    }

    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(clientName,byteBuffer);
        stringEncoding(serverKey,byteBuffer);
        if(!CollectionUtils.isEmpty(this.getContextNames())){
            getContextNames().stream().forEach((contextName)->{
                stringEncoding(contextName,byteBuffer);
            });
        }
    }
}
