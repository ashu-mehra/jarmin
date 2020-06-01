package org.eclipse.openj9.jmin;

public class WorkItem {
    public String clazz;
    public String name;
    public String desc;
    public WorkItem(String clazz, String name, String desc) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
    }
    @Override
    public String toString() {
        return "WorkItem " + clazz + " " + name + " " + desc;
    }
}