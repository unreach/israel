package io.unreach.israel.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http2ChannelHandler extends SimpleChannelInboundHandler<FullHttpResponse> implements ChannelHandler<Channel> {

    private Channel channel;
    private String hostName;

    private final Map<Integer, Map.Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;


    public Http2ChannelHandler(String hostName) {
        this.hostName = hostName;
        streamidPromiseMap = PlatformDependent.newConcurrentHashMap();
    }

    @Override
    public Object invoke(String serviceId, String methodName, Object[] params) {

        HttpScheme scheme = HttpScheme.HTTPS;
        AsciiString hostName = new AsciiString(this.hostName);
        // Create a simple POST request with a body.
        // TODO 序列化 serialize
        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, "/" + serviceId + "_" + methodName,
                wrappedBuffer("todo".getBytes(CharsetUtil.UTF_8)));
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);

        streamidPromiseMap.put(3, new AbstractMap.SimpleEntry<ChannelFuture, ChannelPromise>(channel.write(request), channel.newPromise()));
        channel.flush();

        return null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        if (streamId == null) {
            System.err.println("HttpResponseHandler unexpected message received: " + msg);
            return;
        }

        Map.Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap.get(streamId);
        if (entry == null) {
            System.err.println("Message received for unknown stream id " + streamId);
        } else {
            // Do stuff with the message (for now just print it)
            ByteBuf content = msg.content();
            if (content.isReadable()) {
                int contentLength = content.readableBytes();
                byte[] arr = new byte[contentLength];
                content.readBytes(arr);
                System.out.println(new String(arr, 0, contentLength, CharsetUtil.UTF_8));
            }

            entry.getValue().setSuccess();
        }
    }

    /**
     * Wait (sequentially) for a time duration for each anticipated response
     *
     * @param timeout Value of time to wait for each response
     * @param unit Units associated with {@code timeout}
     * @see HttpResponseHandler#put(int, io.netty.channel.ChannelFuture, io.netty.channel.ChannelPromise)
     */
    public void awaitResponses(long timeout, TimeUnit unit) {
        Iterator<Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>>> itr = streamidPromiseMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Integer, Map.Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
            ChannelFuture writeFuture = entry.getValue().getKey();
            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
                throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey());
            }
            if (!writeFuture.isSuccess()) {
                throw new RuntimeException(writeFuture.cause());
            }
            ChannelPromise promise = entry.getValue().getValue();
            if (!promise.awaitUninterruptibly(timeout, unit)) {
                throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey());
            }
            if (!promise.isSuccess()) {
                throw new RuntimeException(promise.cause());
            }
            System.out.println("---Stream id: " + entry.getKey() + " received---");
            itr.remove();
        }
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
