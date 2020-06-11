package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.tree.analysis.BasicValue;

import org.objectweb.asm.Type;

/**
 * Represents an instance of <code>java/lang/reflect/Method</code>.
 * 
 * The named method is an exact match - eg the reflection object will refer
 * specifically to the named class and method. If the method is invoked on
 * a receiver then usual virtual dispatch or interface dispatch rules will
 * apply and could dispatch to an override of the named method.
 */
public class MethodValue extends BasicValue {
    private String clazz;
    private String name;
    private String desc;
    
    public MethodValue(String clazz, String name, String desc) {
        super(Type.getObjectType("java/lang/reflect/Method"));
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
    }
    
    public MethodValue(MethodValue v) {
        super(Type.getObjectType("java/lang/reflect/Method"));
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