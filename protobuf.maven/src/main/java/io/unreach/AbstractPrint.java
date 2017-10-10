/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.unreach;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author joe
 */
public abstract class AbstractPrint {

    /**
     * 文件路径
     */
    protected String fileRootPath;

    /**
     * 包路径
     */
    protected String sourcePackageName;

    /**
     * 类名称
     */
    protected String className;

    /**
     * pb定义的java_outer_classname
     */
    protected String outClassName;

    /**
     * 是否是多文件编译
     */
    protected boolean isMulti;



    public AbstractPrint(String fileRootPath, String sourcePackageName, String className, String outClassName) {
        this.fileRootPath = fileRootPath;
        this.sourcePackageName = sourcePackageName;
        this.className = className;
        this.outClassName = outClassName;
    }

    public String getClassName() {
        return this.className;
    }

    public String getSourcePackageName() {
        return this.sourcePackageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSourcePackageName(String sourcePackageName) {
        this.sourcePackageName = sourcePackageName;
    }

    protected abstract List<String> collectFileData();

    public void print() {
        List<String> fileData = collectFileData();
        String fileName = fileRootPath + "/" + StringUtils.replace(sourcePackageName.toLowerCase(), ".", "/") + "/"
                + className + ".java";
        File javaFile = new File(fileName);
        if (fileData != null) {
            try {
                FileUtils.writeLines(javaFile, "UTF-8", fileData);
            } catch (IOException e) {
                throw new IllegalArgumentException("can not write file to" + fileName, e);
            }
        }
    }

}
