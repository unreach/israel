package io.unreach.israel.transport;

import io.unreach.israel.ServiceDefine;

public class MessageKey {

    private String msgId;
    private ServiceDefine serviceDefine;
    private String methodName;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public ServiceDefine getServiceDefine() {
        return serviceDefine;
    }

    public void setServiceDefine(ServiceDefine serviceDefine) {
        this.serviceDefine = serviceDefine;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
