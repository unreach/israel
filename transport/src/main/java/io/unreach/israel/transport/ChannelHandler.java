package io.unreach.israel.transport;

/**
 * 远程调用执行接口
 *
 * @param <T>
 * @author joe
 */
public interface ChannelHandler<T> {

    /**
     * 执行方法调用
     *
     * @param serviceId
     * @param methodName
     * @param params
     * @return
     */
    public Object invoke(String serviceId, String methodName, Object[] params);

    /**
     * 获取客户端的channel 实现
     *
     * @return
     */
    public T getChannel();

}
