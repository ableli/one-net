package com.weyong.onenet.dto;

import com.weyong.aes.WXBizMsgCrypt;
import com.weyong.zip.ByteZipUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;

/**
 * Created by haoli on 2018/4/7.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataPackage extends BasePackage {
    private byte[] data;
    private Boolean zip = false;
    private Boolean aes = false;

    public DataPackage(String contextName, Long sessionId, byte[] currentData, boolean gzip, boolean aes) {
        super(BasePackage.DATA);
        this.setContextName(contextName);
        this.setSessionId(sessionId);
        this.setData(currentData);
        this.setAes(aes);
        this.setZip(gzip);
    }

    public DataPackage(ByteBuf byteBuf) {
        super(BasePackage.DATA);
        this.setContextName(stringDecoding(byteBuf));
        this.setSessionId(byteBuf.readLong());
        int boolValues = (int) byteBuf.readByte();
        aes = boolValues % 2 == 1;
        boolValues = boolValues >> 1;
        zip = boolValues % 2 == 1;
        int dataLength = byteBuf.readInt();
        data = new byte[dataLength];
        byteBuf.readBytes(data);
    }

    public byte[] getRawData() {
        if (aes) {
            data = WXBizMsgCrypt.getDecryptBytes(data);
        }
        if (zip) {
            data = ByteZipUtil.unGzip(data);
        }
        return data;
    }

    public byte[] getData() {
        byte[] output = data;
        if (zip) {
            output = ByteZipUtil.gzip(output);
        }
        if (aes) {
            output = WXBizMsgCrypt.getEncryptBytes(output);
        }
        return output;
    }

    @Override
    protected void fillBody(ByteBuffer byteBuffer) {
        stringEncoding(this.getContextName(), byteBuffer);
        byteBuffer.putLong(getSessionId());
        int boolValues = getBoolValues(zip, aes);
        byteBuffer.put((byte) boolValues);
        byte[] output = getData();
        byteBuffer.putInt(output.length);
        byteBuffer.put(output);
    }


}
