package com.weyong.onenet.dto;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import org.apache.commons.codec.Charsets;

import java.io.Serializable;
import java.nio.*;

/**
 * Created by hao.li on 2017/4/12.
 */
@Data
public abstract class BasePackage implements Serializable{
    ThreadLocal<ByteBuffer> byteBufferThreadLocal = new ThreadLocal<>();
    public static final byte INITIAL_REQUEST = 1;
    public static final byte HEART_BEAT = 2;
    public static final byte INVALID_SESSION = 3;
    public static final byte MESSAGE = 4;
    public static final byte DATA = 5;
    public static final byte INITIAL_RESPONSE = 6;
    private Byte opType;
    private String contextName;
    private Long sessionId;

    public BasePackage(Byte opType){
        this.opType = opType;
    }

    public byte[] toBytes() {
        ByteBuffer byteBuffer = this.getByteBufferThreadLocal().get();
        byteBuffer.clear();
        byteBuffer.put(this.getOpType());
        fillBody(byteBuffer);
        byte[] byteEncoding =  new byte[byteBuffer.position()];
        byteBuffer.flip();
        byteBuffer.get(byteEncoding,0,byteEncoding.length);
        return byteEncoding;
    }

    protected abstract void fillBody(ByteBuffer byteBuffer);


    public static BasePackage fromBytes(ByteBuf byteBuf){
       byte typeByte = byteBuf.readByte();
       switch(typeByte){
           case INITIAL_REQUEST:
               return new InitialRequestPackage(byteBuf);
           case HEART_BEAT:
               return HeartbeatPackage.instance();
           case INVALID_SESSION:
               return new InvalidSessionPackage(byteBuf);
           case MESSAGE:
               return new MessagePackage(byteBuf);
           case DATA:
               return new DataPackage(byteBuf);
           case INITIAL_RESPONSE:
               return new InitialResponsePackage(byteBuf);
       }
       return null;
    }

    protected static void stringEncoding(String str,ByteBuffer byteBuf){
        //All string need to encoding must be less than 128
        if(StringUtil.isNullOrEmpty(str)){
            byteBuf.put((byte)0);
        }else {
            byte[] bytes = str.getBytes(Charsets.toCharset("UTF-8"));
            byteBuf.put((byte)bytes.length);
            byteBuf.put(bytes);
        }
    }

    protected static String stringDecoding(ByteBuf byteBuf){
        byte byteLength = byteBuf.readByte();
        byte[] bytes = new byte[byteLength];
        byteBuf.readBytes(bytes,0,byteLength);
        return new String(bytes,Charsets.toCharset("UTF-8"));
    }

    protected int getBoolValues(boolean zip, boolean aes) {
        int boolValues = zip?1:0;
        boolValues  = boolValues << 1;
        boolValues = aes ? boolValues++:boolValues;
        return boolValues;
    }
}
