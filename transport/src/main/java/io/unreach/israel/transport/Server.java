package io.unreach.israel.transport;

/**
 * the server interface
 *
 * @author joe
 */
public interface Server {

    /**
     * 初始化服务
     *
     * @param port
     */
    public boolean start(int port);

    public void stop();

}
