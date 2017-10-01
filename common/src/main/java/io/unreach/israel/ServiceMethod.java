package io.unreach.israel;

import java.io.Serializable;

/**
 * the service method pojo
 * @author joe
 */
public class ServiceMethod implements Serializable {

    private static final long serialVersionUID = 1930807862934361258L;

    private String methodName;
    private Class[] methodParams;
    private String methodDesc;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(Class[] methodParams) {
        this.methodParams = methodParams;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }
}
