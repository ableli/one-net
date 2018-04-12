package com.weyong.onenet.server.handler;

import com.weyong.onenet.server.context.OneNetServerContext;
import com.weyong.onenet.server.context.OneNetServerHttpContext;
import com.weyong.onenet.server.session.OneNetHttpSession;
import com.weyong.onenet.server.session.OneNetSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by haoli on 12/26/2017.
 */
@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<Object> {
    private OneNetServerHttpContext httpContext;
    private OneNetHttpSession httpSession;

    public HttpRequestHandler(OneNetServerHttpContext oneNetServerContext, OneNetHttpSession httpSession) {
        this.httpContext = oneNetServerContext;
        this.httpSession = httpSession;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            HttpHeaders headers = request.headers();
            String hostName = headers.get("HOST");
            if (StringUtils.isNotEmpty(hostName)) {
                if (httpSession.getOneNetChannel() == null) {
                    Channel clientChannel = httpContext.getOneNetConnectionManager().getAvailableChannel(hostName);
                    if (clientChannel != null) {
                        httpSession.setOneNetChannel(clientChannel);
                    }
                }
            }
        }
        if(httpSession.getOneNetChannel() != null){
            while(httpSession.getQueue().size()>0){
                //log.info("Http Data Trans Dequeue.");
                httpSession.getOneNetChannel().writeAndFlush(httpSession.getQueue().poll());
            }
        }else {
            log.info(String.format("Can't find client session for http context %s.",
                    httpContext.getOneNetServerContextConfig().getContextName()));
            ctx.close();
        }
    }
}
