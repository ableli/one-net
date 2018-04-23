package com.weyong.onenet.dto;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The abstract object of OneNet message.
 * It also contains a set of util methods.
 *
 * Created by hao.li on 2017/4/12.
 */
@Data
@Slf4j
public abstract class BasePackage implements Serializable {
    public static final byte INITIAL_REQUEST = 1;
    public static final byte HEART_BEAT = 2;
    public static final byte INVALID_SESSION = 3;
    public static final byte MESSAGE = 4;
    public static final byte DATA = 5;
    public static final byte INITIAL_RESPONSE = 6;
    private static final ThreadLocal<ByteBuffer> byteBufferThreadLocal = new ThreadLocal<>();
    private Byte msgType;
    private String contextName;
    private Long sessionId;

    /**
    * Constructor for all sub-class.
    */
    protected BasePackage(Byte msgType) {
        this.msgType = msgType;
    }

    /**
     * Return a BasePackage sub class instance, the instance come from the incoming frame Bytebuf.
     * The first byte of the msg byte array is the message type.
     * @param byteBuf frame data incoming
     * @return The sub class instance of BasePackage
     */
    public static BasePackage fromBytes(ByteBuf byteBuf) {
        byte typeByte = byteBuf.readByte();
        switch (typeByte) {
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

    /**
     * Encoding a string to byte buffer.
     * The first is the string byte array length ,
     * So the String byte array length should not exced 128.
     * The empty string will encoding as lenght 0.
     * @param str
     * @param byteBuf
     */
    protected static void stringEncoding(String str, ByteBuffer byteBuf) {
        //All string need to encoding must be less than 128
        if (StringUtil.isNullOrEmpty(str)) {
            byteBuf.put((byte) 0);
        } else {
            byte[] bytes = str.getBytes(Charsets.toCharset("UTF-8"));
            if(bytes.length<128) {
                byteBuf.put((byte) bytes.length);
                byteBuf.put(bytes);
            }else{
                log.warn("String byte array length exced 128, can't do encoding.");
                byteBuf.put((byte)128);
                for(int i=0;i<128;i++){
                    byteBuf.put(bytes[i]);
                }
            }
        }
    }

    /**
     * Decoding string from byte buffer.
     * The first byte is the length of the String byte array.
     * The lenght byte zero , means the String is empty.
     * @param byteBuf
     * @return String Decoded.
     */
    protected static String stringDecoding(ByteBuf byteBuf) {
        byte byteLength = byteBuf.readByte();
        byte[] bytes = new byte[byteLength];
        byteBuf.readBytes(bytes, 0, byteLength);
        return new String(bytes, Charsets.toCharset("UTF-8"));
    }

    /**
     * Transfer current instance to a byte array.
     * There is a 1M byte buffer in ThreadLocal to reuse.
     * @return byte array repersent the instance.
     */
    public byte[] toBytes() {
        if (byteBufferThreadLocal.get() == null) {
            byteBufferThreadLocal.set(ByteBuffer.allocate(1048576));
        }
        ByteBuffer byteBuffer = byteBufferThreadLocal.get();
        byteBuffer.clear();
        byteBuffer.put(this.getMsgType());
        fillBody(byteBuffer);
        byte[] byteEncoding = new byte[byteBuffer.position()];
        byteBuffer.flip();
        byteBuffer.get(byteEncoding, 0, byteEncoding.length);
        return byteEncoding;
    }

    /**
     * Sub class instance wirte their info to the byte buffer.
     * The different sub class got different protocol to encoding theirself.
     * @param byteBuffer
     */
    protected abstract void fillBody(ByteBuffer byteBuffer);

    /**
     * Encoding the boolean values to an int,
     * [true,false ,true] will resulte the last byte of int look like 0000,0101
     * The length of bools should not be large then 32.
     * @param zip
     * @param aes
     * @return
     */
    public static int getBoolValues(boolean... bools) {
        if(bools.length == 0 || bools.length > 32) {
            return 0;
        }
        int boolValues = 0;
        for(int i=0;i<bools.length;i++) {
            boolValues = boolValues << 1;
            boolValues = bools[0] ? boolValues++ : boolValues;
        }
        return boolValues;
    }
}
