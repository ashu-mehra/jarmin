package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.tree.analysis.BasicValue;

public class MethodValue extends BasicValue {
    private String clazz;
    private String name;
    private String desc;
    
    public MethodValue(String clazz, String name, String desc) {
        super(null);
        if (clazz.equals("com.arjuna.ats.internal.jta.Implementationsx"))
        throw new RuntimeException();
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
    }
    
    public MethodValue(MethodValue v) {
        super(null);
        this.clazz = v.clazz;
        this.name = v.name;
        this.desc = v.desc;
    }
    
    public String getClazz() {
        return clazz;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof MethodValue) {
            MethodValue m = ((MethodValue)o);
            return m.clazz.equals(clazz) && m.name.equals(name) && m.desc.equals(desc);
        }
        return false;
    }
}