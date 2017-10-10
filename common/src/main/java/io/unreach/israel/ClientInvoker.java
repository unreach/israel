package io.unreach.israel;

public interface ClientInvoker {

    public <R> R invoke(String service,Object param);

}
