/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.unreach;

import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shimingliu 2016年12月19日 下午3:14:06
 * @version GenerateFileUtil.java, v 0.0.1 2016年12月19日 下午3:14:06 shimingliu
 */
public final class PrintEnumFile extends AbstractPrint {

    private List<EnumValueDescriptorProto> enumFields;

    public PrintEnumFile(String fileRootPath, String sourcePackageName, String className,String outerClassName){
        super(fileRootPath, sourcePackageName, className,outerClassName);
    }

    public void setEnumFields(List<EnumValueDescriptorProto> enumFields) {
        this.enumFields = enumFields;
    }

    @Override
    protected List<String> collectFileData() {
        String className = getClassName();
        String packageName = getSourcePackageName() + ".model";
        setSourcePackageName(packageName);


        List<String> fileData = new ArrayList<>();
        fileData.add("package " + packageName + ";");
        fileData.add("public enum " + className + "{");
        for (int i = 0; i < enumFields.size(); i++) {
            EnumValueDescriptorProto enumField = enumFields.get(i);
            if (i == enumFields.size() - 1) {
                fileData.add("    "+enumField.getName() + "(" + enumField.getNumber() + ");");
            } else {
                fileData.add("    "+enumField.getName() + "(" + enumField.getNumber() + "),");
            }
        }
        fileData.add("    private final int value;");
        fileData.add("    private " + className + "(int value){");
        fileData.add("        this.value = value;");
        fileData.add("    }");
        fileData.add("    public final int getNumber() {");
        fileData.add("        return value;");
        fileData.add("    }");
        fileData.add("    public static " + className + " forNumber(Integer value){");
        fileData.add("        switch (value) {");
        for (int i = 0; i < enumFields.size(); i++) {
            EnumValueDescriptorProto enumField = enumFields.get(i);
            if (i == enumFields.size() - 1) {
                fileData.add("            case " + enumField.getNumber() + ":");
                fileData.add("                return " + enumField.getName() + ";");
                fileData.add("            default:");
                fileData.add("                return null;");
            } else {
                fileData.add("            case " + enumField.getNumber() + ":");
                fileData.add("                return " + enumField.getName() + ";");
            }
        }
        fileData.add("        }");
        fileData.add("    }");
        fileData.add("}");
        return fileData;
    }

}
