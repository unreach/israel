package io.unreach.israel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * service define pojo
 *
 * @author joe
 */
public class ServiceDefine implements Serializable {
    private static final long serialVersionUID = 6123631505988828805L;

    // example: io.unreash.israel.helloword
    private String serviceName;
    private String applicationName = "default";
    private String groupName = "default";
    private String version = "1.0.0";
    // 响应超时时间，单位ms,默认30s
    private long responseTimeout = 30000;
    // 缓存暴露服务的方法定义，不允许重载
    private Map<String, ServiceMethod> methods = new HashMap<String, ServiceMethod>();

    private List<ServiceProvider> providers = new ArrayList<>();

    private Object serviceProviderBean;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public Map<String, ServiceMethod> getMethods() {
        return methods;
    }

    public Object getServiceProviderBean() {
        return serviceProviderBean;
    }

    public void setServiceProviderBean(Object serviceProviderBean) {
        this.serviceProviderBean = serviceProviderBean;
    }

    /**
     * get a method
     *
     * @param methodName
     * @return
     */
    public ServiceMethod getMethod(String methodName) {
        return methods.get(methodName);
    }

    /**
     * add a method
     *
     * @param method
     */
    public void addMethod(ServiceMethod method) {
        methods.put(method.getMethodName(), method);
    }

    public void addProvider(ServiceProvider provider) {
        this.providers.add(provider);
    }

    public List<ServiceProvider> getProviders() {
        return providers;
    }

    /**
     * 获取唯一id
     * @return
     */
    public String getId() {
        return serviceName + "_" + version;
    }
}
