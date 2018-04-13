package com.weyong.onenet.server.handler;

import com.weyong.onenet.dto.DataPackage;
import com.weyong.onenet.server.Initializer.HttpChannelInitializer;
import com.weyong.onenet.server.context.OneNetServerHttpContext;
import com.weyong.onenet.server.session.OneNetHttpSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by hao.li on 2017/4/12.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class HttpRawDataHandler extends HttpObjectDecoder {
    private OneNetHttpSession httpSession;
    private String hostName;

    public HttpRawDataHandler(OneNetHttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        int length =  in.readableBytes();
        byte[] currentData = new byte[length];
        in.readBytes(currentData, 0, length);
        in.resetReaderIndex();
        super.channelRead(ctx, msg);
        if (httpSession.getOneNetChannel() != null) {
            httpSession.getOneNetChannel().writeAndFlush(new DataPackage(
                    httpSession.getContextName(),
                    httpSession.getSessionId(),
                    currentData,
                    httpSession.getContextConfig().isZip(),
                    httpSession.getContextConfig().isAes()));

            int kBps = httpSession.getContextConfig().getKBps()*1024;
            ChannelTrafficShapingHandler trafficShapingHandler =  ((ChannelTrafficShapingHandler) ctx.pipeline().get(HttpChannelInitializer.trafficHandler));
            trafficShapingHandler.setWriteLimit(kBps);
            trafficShapingHandler.setReadLimit(kBps);
        } else {
            log.info(String.format("Can't find client session for http context %s.",
                    hostName));
            ctx.close();
        }
        in.release();
    }

    @Override
    protected boolean isDecodingRequest() {
        return StringUtils.isEmpty(httpSession.getContextName());
    }

    @Override
    protected HttpMessage createMessage(String[] initialLine) throws Exception {
        HttpMessage msg =  new DefaultHttpRequest(
                HttpVersion.valueOf(initialLine[2]),
                HttpMethod.valueOf(initialLine[0]), initialLine[1], validateHeaders);
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            HttpHeaders headers = request.headers();
            hostName = headers.get("HOST");
            if (StringUtils.isNotEmpty(hostName)) {
                httpSession.setContextConfig(OneNetServerHttpContext.instance.getContextConfig(hostName));
                if (httpSession.getContextConfig() != null && httpSession.getOneNetChannel() == null) {
                    Channel clientChannel = OneNetServerHttpContext.instance.getOneNetConnectionManager().getAvailableChannel(hostName);
                    if (clientChannel != null) {
                        httpSession.setOneNetChannel(clientChannel);
                    }
                }
            }
        }
        return msg;
    }

    @Override
    protected HttpMessage createInvalidMessage() {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/bad-request", validateHeaders);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        OneNetServerHttpContext.instance.close(httpSession);
        super.channelInactive(ctx);
    }
}
