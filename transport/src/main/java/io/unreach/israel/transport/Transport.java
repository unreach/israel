package io.unreach.israel.transport;

import io.unreach.israel.ServiceDefine;
import io.unreach.israel.ServiceDefineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

/**
 * 网络传输API
 *
 * @author joe
 */
public class Transport {

    private static final Logger logger = LoggerFactory.getLogger(Transport.class);

    /**
     * http://api.israel.unreach.io/default/io/unreach/israel/helloworld_say
     *
     * @param serviceId  serviceName+serviceVersion
     * @param methodName
     * @param params
     * @return
     */
    public static Object send(String serviceId, String methodName, Object[] params) {
        Channel channel = ChannelFactory.getChannel(serviceId);
        Object result = channel.getHandler().invoke(serviceId, methodName, params);
        return result;
    }

    public static Object invoke(String serviceId, String methodName, Object[] params) {
        ServiceDefine serviceDefine = ServiceDefineFactory.getServiceDefine(serviceId);
        Object instance = serviceDefine.getServiceProviderBean();

        for (Method method : instance.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                int paramLength = 0;
                if (params != null) {
                    paramLength = params.length;
                }
                int paramCount = 0;
                if (method.getParameterTypes() != null) {
                    paramCount = method.getParameterCount();
                }
                if (paramLength !=paramCount) {
                    continue;
                }
                try {
                    Object result = method.invoke(instance, params);
                    return result;
                } catch (Exception e) {
                    logger.error("invoke the service:" + serviceId + "_" + methodName + " error", e);
                    return null;
                }
            }
        }

        return null;
    }

}
