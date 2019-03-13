package org.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class WebSocketServer {

    @PostConstruct
    public void startup() throws Exception {
        new Thread(new Runnable() {
            public void run() {
                try {
                    int port = 8090;
                    new WebSocketServer().run(port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        int port = 8090;
        new WebSocketServer().run(port);
    }

    private void run(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new MyChannelInitializer());

            Channel ch = b.bind(port).sync().channel();
            System.out.println("Web Socket server started at port " + port);
            System.out.println("Open your browser and navigate to http://localhost:" + port);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("http-codec", new HttpServerCodec());
            pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
            pipeline.addLast("handler", new WebSocketServerHandler());
        }
    }
}