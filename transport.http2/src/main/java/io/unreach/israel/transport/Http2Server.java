package io.unreach.israel.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2MultiplexCodec;
import io.netty.handler.codec.http2.Http2MultiplexCodecBuilder;
import io.unreach.israel.transport.internal.server.ChannelHttp2Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http2Server extends ChannelInitializer<SocketChannel> implements Server {

    private static final Logger logger = LoggerFactory.getLogger(Http2Server.class);

    private Channel channel;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup(10);

    @Override
    public boolean start(int port) {
        // Configure the server.
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //.handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(this);
        try {
            channel = b.bind(port).sync().channel();
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }

       // System.err.println("Open your HTTP/2-enabled web browser and navigate to https://127.0.0.1:" + port + '/');

        return true;

    }

    @Override
    public void stop() {
        if(channel==null){
            return;
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
        bossGroup = null;
        workerGroup = null;
        channel = null;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Http2MultiplexCodec codec = Http2MultiplexCodecBuilder.forServer(new ChannelHttp2Handler()).build();
        ch.pipeline().addLast(SslUtils.getServerSsl().newHandler(ch.alloc()), codec);
    }

}
