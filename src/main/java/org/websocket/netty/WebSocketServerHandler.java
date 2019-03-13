package org.websocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handshaker;

    @Override
    public void messageReceived(ChannelHandlerContext context, Object msg) throws Exception {
        //Http接入
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(context, (FullHttpRequest) msg);
        }
        //WebSocket接入
        else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(context, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) throws Exception {
        context.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) throws Exception {
        throwable.printStackTrace();
        context.close();
    }

    private void handleHttpRequest(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        if (!request.getDecoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            sendHttpResponse(context, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory("http://localhost:8080/websocket", null, false);
        handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(context.channel());
        } else {
            handshaker.handshake(context.channel(), request);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext context, WebSocketFrame frame) {
        //判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(context.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        //判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            context.channel().write(new PingWebSocketFrame(frame.content().retain()));
            return;
        }

        //本例仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(frame.getClass().getName() + "frame type not supported.");
        }

        //返回应答消息
        String request = ((TextWebSocketFrame) frame).text();

        for (int i = 0; i < 10; i++) {
            context.channel().writeAndFlush(new TextWebSocketFrame(request + " ,欢迎使用Netty Websocket服务" + Integer.toString(i)));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendHttpResponse(ChannelHandlerContext context, FullHttpRequest request, FullHttpResponse response) {
        //返回应答给客户端
        if (response.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            setContentLength(request, response.content().readableBytes());
        }

        //如果是非Keep-Alive，关闭连接
        ChannelFuture future = context.channel().writeAndFlush(request);
        if (!isKeepAlive(request) || response.getStatus().code() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
