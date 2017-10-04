package io.unreach.israel.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.unreach.israel.ServiceProvider;
import io.unreach.israel.exception.TimeoutException;
import io.unreach.israel.transport.internal.client.Http2ClientInitializer;
import io.unreach.israel.transport.internal.client.Http2SettingsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * netty http2 client
 *
 * @author joe
 */
public class Http2Client implements Client {

    private static final Logger logger = LoggerFactory.getLogger(Http2Client.class);

    private final static ConcurrentHashMap<String, EventLoopGroup> eventLoopGroups = new ConcurrentHashMap<>();

    @Override
    public Channel connect(ServiceProvider provider) throws TimeoutException {
        Channel israelChannel = new Channel();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        eventLoopGroups.put(provider.getId(), workerGroup);
        // Configure the client.
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.remoteAddress(provider.getHost(), provider.getPort() == 0 ? 80 : provider.getPort());

        Http2ChannelHandler channelHandler = new Http2ChannelHandler(provider.getId());
        Http2ClientInitializer initializer = new Http2ClientInitializer(channelHandler, SslUtils.getClientSsl(), Integer.MAX_VALUE);
        b.handler(initializer);

        israelChannel.setProvider(provider);
        // Start the client.
        try {
            io.netty.channel.Channel channel = b.connect().syncUninterruptibly().channel();
            Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
            http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

            channelHandler.setChannel(channel);
            israelChannel.setHandler(channelHandler);
            israelChannel.setStatus(ChannelStatus.CONNECTED);

        } catch (Exception e) {
            logger.error("connect server " + provider.getId() + " error:", e);
            israelChannel.setStatus(ChannelStatus.DISCONNECTED);
        }
        return israelChannel;
    }

    @Override
    public void destory(Channel channel) {
        io.netty.channel.Channel channel1 = (io.netty.channel.Channel) channel.handler.getChannel();
        channel1.close().syncUninterruptibly();
        eventLoopGroups.get(channel.getProvider().getId()).shutdownGracefully();
    }
}
