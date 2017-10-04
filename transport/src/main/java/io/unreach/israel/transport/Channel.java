package io.unreach.israel.transport;

import io.unreach.israel.ServiceProvider;

/**
 * israle://groupName/serviceName/methodName/version
 *
 * @author joe
 */
public class Channel {

    public ChannelStatus status;

    public ServiceProvider provider;

    public ChannelHandler handler;


    public ChannelStatus getStatus() {
        return status;
    }

    public void setStatus(ChannelStatus status) {
        this.status = status;
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public void setProvider(ServiceProvider provider) {
        this.provider = provider;
    }

    public ChannelHandler getHandler() {
        return handler;
    }

    public void setHandler(ChannelHandler handler) {
        this.handler = handler;
    }

}
