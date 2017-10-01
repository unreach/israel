package io.unreach.israel;

import java.util.HashMap;
import java.util.Map;

public class ServiceDefineFactory {

    private static Map<String, ServiceDefine> refences = new HashMap<>();
    private static Map<String, ServiceDefine> exports = new HashMap<>();

    public static ServiceDefine getServiceDefine(String serviceId) {
        return refences.get(serviceId);
    }

    public static void addRefence(ServiceDefine serviceDefine){
        refences.putIfAbsent(serviceDefine.getId(),serviceDefine);
    }

    public static void addExport(ServiceDefine serviceDefine){
        exports.putIfAbsent(serviceDefine.getId(),serviceDefine);
    }

}
