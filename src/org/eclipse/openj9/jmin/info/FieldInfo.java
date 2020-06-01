package org.eclipse.openj9.jmin.info;

import java.util.HashSet;

public class FieldInfo {
    private final  String name;
    private final String desc;
    private HashSet<String> annotations;
    boolean referenced;

    public FieldInfo(String name, String desc) {
        this.name = name;
        this.desc = desc;
        this.referenced = false;
        this.annotations = new HashSet<String>();
    }
    public String name() {
        return name;
    }
    public String desc() {
        return desc;
    }
    public void setReferenced() {
        this.referenced = true;
    }
    public boolean referenced() {
        return referenced;
    }
    public void addAnnotation(String annot) {
        annotations.add(annot);
    }
    public HashSet<String> getAnnotations() {
        return annotations;
    }
    @Override
    public String toString() {
        return name + desc + " -" + (referenced ? "" : " not") + " referenced";
    }
}