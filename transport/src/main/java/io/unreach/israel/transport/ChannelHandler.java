package io.unreach.israel.transport;

public interface ChannelHandler {

    public Object invoke(String serviceId, String methodName, Object[] params);

}
