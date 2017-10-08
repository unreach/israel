package io.unreach.israel.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http2ChannelHandler extends SimpleChannelInboundHandler<FullHttpResponse> implements ChannelHandler<Channel> {

    private Channel channel;
    private String hostName;

    private final Map<String, Map.Entry<ChannelFuture, ChannelPromise>> streamidPromiseMap;

    private final static ConcurrentHashMap resultCache = new ConcurrentHashMap();

    public Http2ChannelHandler(String hostName) {
        this.hostName = hostName;
        streamidPromiseMap = PlatformDependent.newConcurrentHashMap();
    }

    @Override
    public Object invoke(String serviceId, String methodName, Object[] params) {

        String msgId = UUID.randomUUID().toString();

        HttpScheme scheme = HttpScheme.HTTPS;
        AsciiString hostName = new AsciiString(this.hostName);
        // Create a simple POST request with a body.
        // TODO 序列化 serialize
        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, serviceId + "_" + methodName,
                wrappedBuffer(methodName.getBytes(CharsetUtil.UTF_8)));
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);

        request.headers().add(TransportConstants.MSG_ID, msgId);

        ChannelFuture future = channel.writeAndFlush(request);
        ChannelPromise promise = future.channel().newPromise();

        streamidPromiseMap.put(msgId, new AbstractMap.SimpleEntry<ChannelFuture, ChannelPromise>(future, promise));

        promise.awaitUninterruptibly();
        return resultCache.get(msgId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        String msgId = msg.headers().get(TransportConstants.MSG_ID);

        Map.Entry<ChannelFuture, ChannelPromise> entry = streamidPromiseMap.get(msgId);
        if (entry == null) {
            System.err.println("Message received for unknown msgId id " + msgId);
        } else {
            // Do stuff with the message (for now just print it)
            ByteBuf content = msg.content();
            if (content.isReadable()) {
                int contentLength = content.readableBytes();
                byte[] arr = new byte[contentLength];
                content.readBytes(arr);
                resultCache.put(msgId, new String(arr, 0, contentLength, CharsetUtil.UTF_8));
            }
            //修改 promise状态
            entry.getValue().setSuccess();
        }
    }

//    /**
//     * Wait (sequentially) for a time duration for each anticipated response
//     *
//     * @param timeout Value of time to wait for each response
//     * @param unit    Units associated with {@code timeout}
//     */
//    public void awaitResponses(long timeout, TimeUnit unit) {
//        Iterator<Map.Entry<String, Map.Entry<ChannelFuture, ChannelPromise>>> itr = streamidPromiseMap.entrySet().iterator();
//        while (itr.hasNext()) {
//            Map.Entry<String, Map.Entry<ChannelFuture, ChannelPromise>> entry = itr.next();
//            ChannelFuture writeFuture = entry.getValue().getKey();
//            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
//                throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey());
//            }
//            if (!writeFuture.isSuccess()) {
//                throw new RuntimeException(writeFuture.cause());
//            }
//            ChannelPromise promise = entry.getValue().getValue();
//            if (!promise.awaitUninterruptibly(timeout, unit)) {
//                throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey());
//            }
//            if (!promise.isSuccess()) {
//                throw new RuntimeException(promise.cause());
//            }
//            System.out.println("---Stream id: " + entry.getKey() + " received---");
//            itr.remove();
//        }
//    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
