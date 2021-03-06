package io.unreach.israel;

import java.io.Serializable;

/**
 * server host pojo
 *
 * @author joe
 */
public class ServiceProvider implements Serializable {

    private static final long serialVersionUID = -8538482418107213047L;

    private String host;
    private int port = 0;

    public ServiceProvider() {

    }

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId() {
        return this.host + ":" + this.port;
    }
}
