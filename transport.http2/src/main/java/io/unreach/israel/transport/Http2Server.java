package io.unreach.israel.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.unreach.israel.transport.server.Http2OrHttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http2Server extends ChannelInitializer<SocketChannel> implements Server {

    private static final Logger logger = LoggerFactory.getLogger(Http2Server.class);

    @Override
    public boolean initialize(int port) {

        // Configure the server.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(this);

            Channel ch = b.bind(port).sync().channel();

            System.err.println("Open your HTTP/2-enabled web browser and navigate to https://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();
            return true;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            group.shutdownGracefully();
        }

        return false;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(SslUtils.getServerSsl().newHandler(ch.alloc()), new Http2OrHttpHandler());
    }

}
