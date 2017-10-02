package io.unreach.israel.transport;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;

/**
 * SSL tools
 */
public class SslUtils {

    private static final Logger logger = LoggerFactory.getLogger(SslUtils.class);
    private static SslContext serverSslCtx;
    private static SslContext clientSslCtx;


    private static final ApplicationProtocolConfig applicationProtocolConfig = new ApplicationProtocolConfig(
            ApplicationProtocolConfig.Protocol.ALPN,
            // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
            // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
            ApplicationProtocolNames.HTTP_2,
            ApplicationProtocolNames.HTTP_1_1);

    /**
     * 初始化SSL
     *
     * @return
     */
    public static SslContext getServerSsl() {
        if (serverSslCtx == null) {
            InputStream cert = SslUtils.class.getClassLoader().getResourceAsStream("ssl/ca.pem");
            InputStream key = SslUtils.class.getClassLoader().getResourceAsStream("ssl/server_pkcs8.key");
            // File cert = new File("/Users/joe/work/israel/ssl/ca.pem");
            // File key = new File("/Users/joe/work/israel/ssl/server_pkcs8.key");
            serverSslCtx = init(SslContextBuilder.forServer(cert, key));

        }
        return serverSslCtx;
    }

    private static SslContext init(SslContextBuilder builder) {
        try {
            return builder.sslProvider(SslProvider.OPENSSL)
                        /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                         * Please refer to the HTTP/2 specification for cipher requirements. */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(applicationProtocolConfig)
                    .build();
        } catch (SSLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化SSL
     *
     * @return
     */
    public static SslContext getClientSsl() {
        if (clientSslCtx == null) {
            InputStream cert = SslUtils.class.getClassLoader().getResourceAsStream("ssl/ca.pem");
            clientSslCtx = init(SslContextBuilder.forClient().trustManager(cert));
        }
        return clientSslCtx;
    }


}
