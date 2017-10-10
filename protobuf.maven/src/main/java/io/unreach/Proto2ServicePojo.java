/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.unreach;

import com.google.protobuf.DescriptorProtos.*;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ProtocolStringList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author shimingliu 2016年12月17日 下午1:43:38
 * @version Proto2Interface.java, v 0.0.1 2016年12月17日 下午1:43:38 shimingliu
 */
public class Proto2ServicePojo {

    private final String discoveryRoot;

    private final String generatePath;

    private final CommondProtoc commondProtoc;

    private Map<String, String> pojoTypes;

    private Proto2ServicePojo(String discoveryRoot, String generatePath) {
        this.discoveryRoot = discoveryRoot;
        this.generatePath = generatePath;
        this.commondProtoc = CommondProtoc.configProtoPath(discoveryRoot);
    }

    public static Proto2ServicePojo forConfig(String discoveryRoot, String generatePath) {
        return new Proto2ServicePojo(discoveryRoot, generatePath);
    }

    public void generateFile(String protoPath) {
        try {
            if (pojoTypes == null) {
                pojoTypes = new HashMap<>();
            }
        } finally {
            FileDescriptorSet fileDescriptorSet = commondProtoc.invoke(protoPath);
            for (FileDescriptorProto fdp : fileDescriptorSet.getFileList()) {
                Map packageClassName = this.packageClassName(fdp.getOptions());
                if (packageClassName == null) {
                    continue;
                }
                ProtocolStringList dependencyList = fdp.getDependencyList();
                for (Iterator<String> it = dependencyList.iterator(); it.hasNext(); ) {
                    String dependencyPath = discoveryRoot + "/" + it.next();
                    generateFile(dependencyPath);
                }
                doPrint(fdp,
                        (String) packageClassName.get("packageName"),
                        (String) packageClassName.get("className"),
                        (boolean) packageClassName.get("isMultipleFile"));
            }
        }
    }

    private Map packageClassName(FileOptions options) {
        String packageName = null;
        String className = null;
        boolean isMultipleFile = false;
        for (Map.Entry<FieldDescriptor, Object> entry : options.getAllFields().entrySet()) {
            if (entry.getKey().getName().equals("java_package")) {
                packageName = entry.getValue().toString();
            }
            if (entry.getKey().getName().equals("java_outer_classname")) {
                className = entry.getValue().toString();
            }
            if (entry.getKey().getName().equals("java_multiple_files")) {
                isMultipleFile = (boolean) entry.getValue();
            }
        }
        if (packageName != null && className != null) {

            Map result = new HashMap();
            result.put("packageName", packageName);
            result.put("className", className);
            result.put("isMultipleFile", isMultipleFile);

            return result;

            //return new ImmutablePair<String, String>(packageName, className);
        }
        return null;
    }

    private void doPrint(FileDescriptorProto fdp, String javaPackage, String outerClassName, boolean isMulti) {
        List<DescriptorProto> messageDescList = fdp.getMessageTypeList();
        List<ServiceDescriptorProto> serviceDescList = fdp.getServiceList();
        List<EnumDescriptorProto> enumDescList = fdp.getEnumTypeList();
        printEnum(enumDescList, javaPackage, outerClassName, isMulti);
        printMessage(messageDescList, javaPackage, outerClassName, isMulti);
        printService(serviceDescList, javaPackage, outerClassName, isMulti);
    }

    private void printService(List<ServiceDescriptorProto> serviceDescList, String javaPackage, String outerClassName, boolean isMulti) {
        for (ServiceDescriptorProto serviceDesc : serviceDescList) {
            PrintServiceFile serviceFile = new PrintServiceFile(generatePath, javaPackage, serviceDesc.getName(), isMulti ? outerClassName : "");
            try {
                serviceFile.setServiceMethods(serviceDesc.getMethodList());
                serviceFile.setPojoTypeCache(pojoTypes);
            } finally {
                serviceFile.print();
            }

//            PrintServiceImplFile serviceImplFile = new PrintServiceImplFile(generatePath, javaPackage, serviceDesc.getName() + "_impl",isMulti?outerClassName:"");
//            try {
//                serviceImplFile.setServiceMethods(serviceDesc.getMethodList());
//                serviceImplFile.setPojoTypeCache(pojoTypes);
//            } finally {
//                serviceImplFile.print();
//            }
//
//            PrintServiceServerImplFile serviceServerImplFile = new PrintServiceServerImplFile(generatePath, javaPackage, serviceDesc.getName()+"_service");
//            try {
//                serviceServerImplFile.setServiceMethods(serviceDesc.getMethodList());
//                serviceServerImplFile.setPojoTypeCache(pojoTypes);
//            } finally {
//                serviceServerImplFile.print();
//            }
        }
    }

    private void printMessage(List<DescriptorProto> messageDescList, String javaPackage, String outerClassName, boolean isMulti) {
        for (DescriptorProto messageDesc : messageDescList) {
            String pojoClassType = messageDesc.getName();
            String pojoPackageName = javaPackage;//+ "." + "model";// outerClassName;
            String fullpojoType = pojoPackageName.toLowerCase() + ".model." + pojoClassType;
            pojoTypes.put(pojoClassType, fullpojoType);

            pojoTypes.put(pojoClassType + "_outclass", isMulti ? "" : outerClassName);
            boolean isInner = false;
            if (messageDesc.getEnumTypeList() != null && !messageDesc.getEnumTypeList().isEmpty()) {
                isInner = true;
                printEnum(messageDesc.getEnumTypeList(), javaPackage, outerClassName, isMulti);
            }


            PrintMessageFile messageFile = new PrintMessageFile(generatePath, pojoPackageName, pojoClassType, isMulti ? "" : outerClassName);
            messageFile.isMulti = isMulti;
            messageFile.isInnerEmum = isInner;
            try {
                messageFile.setMessageFields(messageDesc.getFieldList());
                messageFile.setPojoTypeCache(pojoTypes);
                messageFile.setSourceMessageDesc(messageDesc);
            } finally {
                messageFile.print();
            }
        }
    }

    private void printEnum(List<EnumDescriptorProto> enumDescList, String javaPackage, String outerClassName, boolean isMulti) {
        for (EnumDescriptorProto enumDesc : enumDescList) {
            String enumClassType = enumDesc.getName();
            String enumPackageName = javaPackage;//+ "." + outerClassName;
            String fullpojoType = enumPackageName + ".model." + enumClassType;
            pojoTypes.put(enumClassType, fullpojoType);
            pojoTypes.put(enumClassType + "_outclass", outerClassName);
            pojoTypes.put(enumClassType + "_enum", isMulti ? "" : outerClassName);


            PrintEnumFile enumFile = new PrintEnumFile(generatePath, enumPackageName, enumClassType, outerClassName);
            enumFile.isMulti = isMulti;
            try {
                enumFile.setEnumFields(enumDesc.getValueList());
            } finally {
                enumFile.print();
            }
        }
    }

}
