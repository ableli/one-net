package com.weyong.onenet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hao.li on 2017/4/12.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataTransfer implements Serializable{
    public static final short OP_TYPE_NEW = 1;
    public static final short OP_TYPE_HEART_BEAT = 2;
    public static final short OP_TYPE_CLOSE = 3;
    public static final short OP_TYPE_ERROR = 4;
    public static final short OP_TYPE_DATA = 5;
    private String contextName;
    private List<String> contextNames;
    private String clientName;
    private String serverKey;
    private long sessionId;
    private short opType;
    private byte[] data;
    private boolean zip = false;
    private boolean aes = false;

    public DataTransfer(Long sessionId, short opType){
        this.sessionId = sessionId;
        this.opType = opType;
    }

    public DataTransfer(short opTypeError, byte[] bytes) {
        this.opType = opTypeError;
        this.data = bytes;
    }
}
