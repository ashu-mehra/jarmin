package org.eclipse.openj9.jmin.info;

public class FieldSite {
    public final String clazz;
    public final String name;
    public final String desc;
    public final FieldKind kind;
    public FieldSite(String clazz, String name, String desc, FieldKind kind) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.kind = kind;
    }
}