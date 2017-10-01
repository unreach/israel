package io.unreach.israel.transport;

import io.unreach.israel.ServiceDefine;
import io.unreach.israel.ServiceProvider;
import io.unreach.israel.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelFactory {

    private final static Logger logger = LoggerFactory.getLogger(ChannelFactory.class);
    // 缓存 service和channel的映射关系
    private static final Map<String, List<Channel>> serviceChannelCaches = new ConcurrentHashMap<>();
    private static final Map<String, Channel> channelAllCaches = new ConcurrentHashMap<>();


    /**
     * 初始化服务连接通道
     *
     * @param client
     * @param referenceServiceDefines 依赖的服务定义集合
     */
    public static void initClient(Client client, List<ServiceDefine> referenceServiceDefines) {

        for (ServiceDefine serviceDefine : referenceServiceDefines) {
            for (ServiceProvider serviceProvider : serviceDefine.getProviders()) {
                Channel channel = channelAllCaches.computeIfAbsent(serviceProvider.getId(), k -> {
                    try {
                        return client.connect(serviceProvider);
                    } catch (TimeoutException e) {
                        logger.error("connect " + serviceProvider.getHost() + ":" + serviceProvider.getPort() + " timeout", e);
                    }
                    return null;
                });
                if (channel == null) {
                    continue;
                }
                List<Channel> channels = serviceChannelCaches.computeIfAbsent(serviceDefine.getId(), k -> new ArrayList<>());
                channels.add(channel);
            }
        }
    }


    /**
     * 获取service的连接
     *
     * @param serviceId
     * @return
     */
    public static Channel getChannel(String serviceId) {

        List<Channel> channels = serviceChannelCaches.get(serviceId);

        if (channels == null || channels.isEmpty()) {
            return null;
        }
        // TODO load balance
        Channel channel = channels.get(0);

        return channel;
    }

}
