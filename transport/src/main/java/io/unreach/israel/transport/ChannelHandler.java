package io.unreach.israel.transport;

public interface ChannelHandler<T> {

    public Object invoke(String serviceId, String methodName, Object[] params);

    public T getChannel();

}
