/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.unreach;

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shimingliu 2016年12月21日 下午3:39:54
 * @version PrintServiceFile.java, v 0.0.1 2016年12月21日 下午3:39:54 shimingliu
 */
public final class PrintServiceFile extends AbstractPrint {

    private Map<String, String>         pojoTypeCache;

    private List<MethodDescriptorProto> serviceMethods;

    public PrintServiceFile(String fileRootPath, String sourcePackageName, String className,String outerClassName){
        super(fileRootPath, sourcePackageName, className,outerClassName);
    }

    public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
        this.pojoTypeCache = pojoTypeCache;
    }

    public void setServiceMethods(List<MethodDescriptorProto> serviceMethods) {
        this.serviceMethods = serviceMethods;
    }

    @Override
    protected List<String> collectFileData() {
        String className = super.getClassName();
        String packageName = getSourcePackageName();
        List<String> fileData = new ArrayList<>();
        fileData.add("package " + packageName + ";");

        List<String> imports = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        for (MethodDescriptorProto method : serviceMethods) {
            String outPutType = method.getOutputType();
            String inputType = method.getInputType();
            String methodName = method.getName();
            inputType = CommonUtils.findPojoTypeFromCache(inputType, pojoTypeCache);
            outPutType = CommonUtils.findPojoTypeFromCache(outPutType, pojoTypeCache);
            String inputValue = CommonUtils.findNotIncludePackageType(inputType).toLowerCase();

            imports.add("import " + inputType + ";");
            imports.add("import " + outPutType + ";");

            String methodStr = "    public " + StringUtils.substringAfterLast(outPutType, ".") + " " + StringUtils.uncapitalize(methodName) + "("
                               + StringUtils.substringAfterLast(inputType, ".") + " " + inputValue + ");";
            methods.add(methodStr);

        }
        fileData.addAll(imports);
        fileData.add("\n");
        fileData.add("public interface " + className + "{");
        fileData.addAll(methods);
        fileData.add("}");
        return fileData;
    }

}
