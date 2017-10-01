package io.unreach.israel.transport;

import io.unreach.israel.ServiceProvider;
import io.unreach.israel.exception.TimeoutException;

import java.net.URI;

/**
 * 客户端
 *
 * @author joe
 */
public interface Client {

    /**
     * 连接网络
     *
     * @param provider
     */
    public Channel connect(ServiceProvider provider) throws TimeoutException;


}
