package com.alibaba.otter.shared.common.utils.compile.model;

import org.apache.commons.lang.StringUtils;

import com.alibaba.otter.shared.common.utils.RegexUtils;

/**
 * @author simon 2012-10-16 上午10:33:58
 * @version 4.1.0
 */
public class JavaSource {

    private String packageName;
    private String className;
    private String source;

    public JavaSource(String sourceString){
        String className = RegexUtils.findFirst(sourceString, "public class (?s).*?{").split("extends")[0].split("implements")[0].replaceAll(
                                                                                                                                             "public class ",
                                                                                                                                             StringUtils.EMPTY).replace(
                                                                                                                                                                        "{",
                                                                                                                                                                        StringUtils.EMPTY).trim();
        String packageName = RegexUtils.findFirst(sourceString, "package (?s).*?;").replaceAll("package ",
                                                                                               StringUtils.EMPTY).replaceAll(
                                                                                                                             ";",
                                                                                                                             StringUtils.EMPTY).trim();
        this.packageName = packageName;
        this.className = className;
        this.source = sourceString;
    }

    public JavaSource(String packageName, String className, String source){
        this.packageName = packageName;
        this.className = className;
        this.source = source;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toString() {
        return packageName + "." + className;
    }
}
