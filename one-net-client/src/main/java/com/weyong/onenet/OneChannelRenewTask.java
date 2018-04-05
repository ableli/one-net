package com.weyong.onenet;

import com.weyong.onenet.dto.DataTransfer;
import com.weyong.onenet.initializer.OneChannelInitializer;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by haoli on 6/9/2017.
 */
@Data
@Slf4j
@AllArgsConstructor
public class OneChannelRenewTask implements Runnable {
    private  String outsideIp="localhost";
    private  int outsidePort = 81;
    public static Date lastHeartBeatTime;

    @Override
    public void run() {
        while(true) {
            Calendar lastHeartBeatTimeCondition = Calendar.getInstance();
            lastHeartBeatTimeCondition.add(Calendar.SECOND,-5);
            if(lastHeartBeatTime == null || lastHeartBeatTimeCondition.getTime().after(lastHeartBeatTime)) {
                log.info("One channel is inactive.Try to renew one channel.");
                renewOneChannel();
            }
            heatrbeatOneChannel();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void heatrbeatOneChannel() {
        try {
            DataTransfer dt = new DataTransfer();
            dt.setOpType(DataTransfer.OP_TYPE_HEART_BEAT);
            OnenetClient.oneChannel.writeAndFlush(dt);
            log.debug("Heart beat one channel.");
        }catch (Exception ex) {
            log.error("Heart beat one chanel meet ex , and  it is :"+ex.toString());
        }
    }

    private void renewOneChannel() {
        try {
            log.info("Start to renew one channel.");
            Channel socketChannel = OnenetClient.getChannel(outsideIp, outsidePort, new OneChannelInitializer());
            DataTransfer dt = new DataTransfer();
            dt.setOpType(DataTransfer.OP_TYPE_NEW);
            socketChannel.writeAndFlush(dt);
            OnenetClient.oneChannel = socketChannel;
            log.info("One channel reconnected.");
        }catch (Exception ex){
            log.error(String.format("Renew One Channel meet error. ex is :%s",ex.toString()));
        }
    }


}
