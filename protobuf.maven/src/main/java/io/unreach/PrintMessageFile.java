/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.unreach;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;

/**
 * @author shimingliu 2016年12月21日 下午3:42:47
 * @version PrintMessageFile.java, v 0.0.1 2016年12月21日 下午3:42:47 shimingliu
 */
public final class PrintMessageFile extends AbstractPrint {

    private static final Logger logger = LoggerFactory.getLogger(PrintMessageFile.class);

    private List<FieldDescriptorProto> messageFields;

    private Map<String, String> pojoTypeCache;

    private DescriptorProto sourceMessageDesc;

    public boolean isInnerEmum = false;


    public PrintMessageFile(String fileRootPath, String sourcePackageName, String className, String outClassName) {
        super(fileRootPath, sourcePackageName, className, outClassName);
    }

    public void setMessageFields(List<FieldDescriptorProto> messageFields) {
        this.messageFields = messageFields;
    }

    public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
        this.pojoTypeCache = pojoTypeCache;
    }

    public void setSourceMessageDesc(DescriptorProto sourceMessageDesc) {
        this.sourceMessageDesc = sourceMessageDesc;
    }

    @Override
    protected List<String> collectFileData() {
        //String sourePackageName = super.sourcePackageName;
        String pbMessagePackageName = getSourcePackageName();
        String packageName = pbMessagePackageName + ".model";

        if (!isMulti) {
            pbMessagePackageName += "." + outClassName;
        }
        //修改原始包路径，为了生成真实文件
        setSourcePackageName(packageName);


        List<String> fileData = new ArrayList<>();
        fileData.add("package " + packageName + ";");
        fileData.add(System.getProperty("line.separator"));
        fileData.add("import java.util.*;");
        // fileData.add("import com.quancheng.saluki.serializer.ProtobufEntity;");
        fileData.add(System.getProperty("line.separator"));
        // fileData.add("@ProtobufEntity(" + sourePackageName + "." + className + ".class)");
        fileData.add("public class " + getClassName() + " implements io.unreach.israel.BindMessage<" + pbMessagePackageName + "."
                + getClassName() + "," + getClassName() + ">{");
        List<String> pojoData = new ArrayList<>();
        List<String> sets = new ArrayList<>();
        List<String> setInstances = new ArrayList<>();

        for (int i = 0; i < messageFields.size(); i++) {
            FieldDescriptorProto messageField = messageFields.get(i);
            String javaType = findJavaType(packageName, sourceMessageDesc, messageField);
            boolean isList = false;
            boolean isEnum = messageField.getType().equals(TYPE_ENUM);
            if (messageField.getLabel() == Label.LABEL_REPEATED && javaType != null) {
                if (!javaType.contains("Map<")) {
                    javaType = "List<" + javaType + ">";
                    isList = true;
                }
            }
            boolean isObject = false;
            boolean isMap = false;
            if (javaType.startsWith("Map<")) {
                isMap = true;
            } else if (messageField.getType().equals(FieldDescriptorProto.Type.TYPE_MESSAGE)) {
                isObject = true;
            }
            String fieldName = messageField.getName();
            if (StringUtils.contains(fieldName, '_')) {
                fieldName = StringUtils.replaceAll(WordUtils.capitalizeFully(fieldName, '_'), "_", "");
                fieldName = StringUtils.uncapitalize(fieldName);
            } else {
                fieldName = StringUtils.uncapitalize(fieldName);
            }

            fileData.add("    private " + javaType + " " + fieldName + ";");
            pojoData.add("    public " + javaType + " get" + captureName(fieldName) + "(){");
            pojoData.add("        return this." + fieldName + ";");
            pojoData.add("    }");
            pojoData.add(System.getProperty("line.separator"));
            pojoData.add("    public void set" + captureName(fieldName) + "(" + javaType + " " + fieldName + "){");
            pojoData.add("        this." + fieldName + "=" + fieldName + ";");
            pojoData.add("    }");
            pojoData.add(System.getProperty("line.separator"));
            initConvert(sets, setInstances, isEnum, isMap, isObject, isList, fieldName, javaType, pbMessagePackageName,
                    messageField.getTypeName());
        }

        fileData.add(System.getProperty("line.separator"));
        fileData.addAll(pojoData);
        fileData.add("    public " + pbMessagePackageName + "." + getClassName() + " convert(){");
        fileData.add("       " + pbMessagePackageName + "." + getClassName() + ".Builder build = " + pbMessagePackageName + "."
                + getClassName() + ".newBuilder();");
        fileData.addAll(sets);
        fileData.add("        return build.build();");
        fileData.add("    }");

        fileData.add(System.getProperty("line.separator"));

        fileData.add("    public  " + getClassName() + " getInstance(" + pbMessagePackageName + "." + getClassName() + " param){");
        fileData.add("        " + getClassName() + " build = new " + getClassName() + "();");
        fileData.addAll(setInstances);
        fileData.add("        return build;");
        fileData.add("    }");

        fileData.add("}");
        return fileData;
    }

    private void initConvert(List<String> sets, List<String> setInstances, boolean isEnum, boolean isMap,
                             boolean isObject, boolean isList, String fieldName, String javaType,
                             String sourePackageName, String typeName) {

        typeName = StringUtils.substring(typeName, 1);
        String sourceoutPutType = StringUtils.substringBeforeLast(typeName, ".");

        typeName = StringUtils.substringAfterLast(typeName, ".");

        if (isList) {
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            sets.add("          for(int i=0;i<get" + captureName(fieldName) + "().size();i++){");
            boolean curIsObject = StringUtils.contains(javaType, '.');
            String opt = "";
            if (curIsObject) {
                opt = ".convert()";
            }
            String pbType = CommonUtils.findNotIncludePackageType(javaType);

            pbType = StringUtils.replaceAll(pbType, ">", "");

            boolean curIsEnum = pojoTypeCache.containsKey(typeName + "_enum");
            // 是枚举
            if (curIsEnum) {

//                String begin = "          build.set" + captureName(fieldName) + "(" + sourceoutPutType + "." + pojoTypeCache.get(typeName + "_enum");
//                if (isInnerEmum || isMulti) {
//                    begin = "          build.set" + captureName(fieldName) + "(" + sourceoutPutType;
//                }
//                String end = ".forNumber(get" + captureName(fieldName) + "().getNumber()));";
//                sets.add(begin + "." + pbType + end);

                String begin = "              build.add" + captureName(fieldName) + "(" + sourceoutPutType + "." + pojoTypeCache.get(typeName + "_enum");
                if (isInnerEmum || isMulti) {
                    begin = "              build.add" + captureName(fieldName) + "(" + sourceoutPutType;
                }
                String end = ".forNumber(get" + captureName(fieldName) + "().get(i).getNumber()));";
                sets.add(begin + "." + pbType + end);
            } else {
                sets.add("            build.add" + captureName(fieldName) + "(get" + captureName(fieldName)
                        + "().get(i)" + opt + ");");
            }

            sets.add("          }");
            sets.add("        }");

            setInstances.add("        if(param.get" + captureName(fieldName) + "Count()>0){");
            if (curIsObject) {
                setInstances.add("            build.set" + captureName(fieldName) + "(new ArrayList<>());");
                setInstances.add("            for(int i=0;i<param.get" + captureName(fieldName) + "Count();i++){");
                String type = javaType.replaceAll("List<", "").replaceAll(">", "");
                if (curIsEnum) {
                    setInstances.add("                build.get" + captureName(fieldName) + "().add(" + type
                            + ".forNumber(param.get" + captureName(fieldName) + "(i).getNumber()));");
                } else {
                    setInstances.add("            " + type + " ins = new " + type + "();");
                    setInstances.add("            build.get" + captureName(fieldName) + "().add(ins.getInstance(param.get" + captureName(fieldName) + "(i)));");
                }
                setInstances.add("            }");
            } else {
                setInstances.add("            build.set" + captureName(fieldName) + "(param.get"
                        + captureName(fieldName) + "List().subList(0,param.get" + captureName(fieldName)
                        + "Count()));");
            }
            setInstances.add("        }");
        } else if (isMap) {
            String type = javaType.replaceAll("Map<", "").replaceAll(">", "");
            String entryType = type.split(",")[1];
            String pbType = CommonUtils.findNotIncludePackageType(entryType);
            if (StringUtils.equals("String", pbType) || StringUtils.equals("Integer", pbType)
                    || StringUtils.equals("Long", pbType) || StringUtils.equals("Double", pbType)
                    || StringUtils.equals("Float", pbType) || StringUtils.equals("Boolean", pbType)) {
                pbType = null;
            }
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            if (pbType == null) {
                sets.add("          build.putAll" + captureName(fieldName) + "(get" + captureName(fieldName) + "());");
            } else {
                sets.add("          get" + captureName(fieldName) + "().forEach((key, value) -> {");
                sets.add("            build.get" + captureName(fieldName) + "().put(key, value.convert());");
                sets.add("          });");
            }
            sets.add("        }");
            if (pbType == null) {
                setInstances.add("        build.set" + captureName(fieldName) + "(param.get" + captureName(fieldName)
                        + "());");
            } else {
                setInstances.add("        build.set" + captureName(fieldName) + "(new HashMap());");
                setInstances.add("        param.get" + captureName(fieldName) + "().forEach((key, value) -> {");
                setInstances.add("          build.get" + captureName(fieldName) + "().put(key, (new " + pbType
                        + "()).getInstance(value));");
                setInstances.add("        });");
            }
        } else if (isObject) {
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            sets.add("            build.set" + captureName(fieldName) + "(get" + captureName(fieldName)
                    + "().convert());");
            sets.add("        }");

            setInstances.add("        if(param.has" + captureName(fieldName) + "()){");
            setInstances.add("            build.set" + captureName(fieldName) + "((new " + javaType + "()).getInstance("
                    + "param.get" + captureName(fieldName) + "()));");
            setInstances.add("        }");
        } else if (isEnum) {
            String pbType = CommonUtils.findNotIncludePackageType(javaType);
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");

            String begin = "          build.set" + captureName(fieldName) + "(" + sourceoutPutType + "." + pojoTypeCache.get(typeName + "_enum");
            if (isInnerEmum || isMulti) {
                begin = "          build.set" + captureName(fieldName) + "(" + sourceoutPutType;
            }
            String end = ".forNumber(get" + captureName(fieldName) + "().getNumber()));";
            sets.add(begin + "." + pbType + end);
            sets.add("        }");

            setInstances.add("        build.set" + captureName(fieldName) + "(" + javaType + ".forNumber(param.get"
                    + captureName(fieldName) + "().getNumber()));");
        } else {
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            sets.add("          build.set" + captureName(fieldName) + "(get" + captureName(fieldName) + "());");
            sets.add("        }");

            // setInstances.add(" if(param.get" + captureName(fieldName) + "()!=null){");

            setInstances.add("        build.set" + captureName(fieldName) + "(param.get" + captureName(fieldName)
                    + "());");
            // setInstances.add(" }");

        }
    }

    private String findJavaType(String packageName, DescriptorProto sourceMessageDesc, FieldDescriptorProto field) {
        switch (field.getType()) {
            case TYPE_ENUM:
                return getMessageJavaType(packageName, sourceMessageDesc, field);
            case TYPE_MESSAGE:
                String javaType = getMessageJavaType(packageName, sourceMessageDesc, field);
                return javaType;
            case TYPE_GROUP:
                logger.info("group have not support yet");
                return null;
            case TYPE_STRING:
                return "String";
            case TYPE_INT64:
                return "Long";
            case TYPE_INT32:
                return "Integer";
            case TYPE_FIXED32:
                return "Integer";
            case TYPE_FIXED64:
                return "Long";
            case TYPE_BOOL:
                return "Boolean";
            case TYPE_DOUBLE:
                return "Double";
            case TYPE_FLOAT:
                return "Float";
            default:
                logger.info("have not support this type " + field.getType()
                        + ",please contact 297442500@qq.com for support");
                return null;
        }
    }

    private String getMessageJavaType(String packageName, DescriptorProto sourceMessageDesc,
                                      FieldDescriptorProto field) {
        String fieldType = CommonUtils.findNotIncludePackageType(field.getTypeName());
        Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> nestedFieldType = transform(sourceMessageDesc);
        // isMap
        if (nestedFieldType.containsKey(fieldType)) {
            Pair<DescriptorProto, List<FieldDescriptorProto>> nestedFieldPair = nestedFieldType.get(fieldType);
            if (nestedFieldPair.getRight().size() == 2) {
                DescriptorProto mapSourceMessageDesc = nestedFieldPair.getLeft();
                List<FieldDescriptorProto> mapFieldList = nestedFieldPair.getRight();
                String nestedJavaType = "Map<" + findJavaType(packageName, mapSourceMessageDesc, mapFieldList.get(0))
                        + "," + findJavaType(packageName, mapSourceMessageDesc, mapFieldList.get(1))
                        + ">";
                return nestedJavaType;
            } else {
                return null;
            }
        } else {
            String result = CommonUtils.findPojoTypeFromCache(field.getTypeName(), pojoTypeCache);
            if (result != null) {
                return result;
            }
            return fieldType;//
        }
    }

    private Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> transform(DescriptorProto sourceMessageDesc) {
        Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> nestedFieldMap = new HashMap<>();
        sourceMessageDesc.getNestedTypeList().forEach(new Consumer<DescriptorProto>() {

            @Override
            public void accept(DescriptorProto t) {
                nestedFieldMap.put(t.getName(),
                        new ImmutablePair<DescriptorProto, List<FieldDescriptorProto>>(t, t.getFieldList()));
            }

        });
        return nestedFieldMap;
    }

    private String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }
}
