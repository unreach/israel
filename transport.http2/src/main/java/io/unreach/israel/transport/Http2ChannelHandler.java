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
import java.util.Map;

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

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
}
