package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.tree.analysis.BasicValue;

import org.objectweb.asm.Type;

/**
 * Represents an instance of <code>java/lang/reflect/Field</code>.
 * 
 * The named field is an exact match - eg the reflection object will refer
 * specifically to the named class and field.
 */
public class FieldValue extends BasicValue {
    private String clazz;
    private String name;
    
    public FieldValue(String clazz, String name) {
        super(Type.getObjectType("java/lang/reflect/Field"));
        this.clazz = clazz;
        this.name = name;
    }
    
    public FieldValue(FieldValue v) {
        super(Type.getObjectType("java/lang/reflect/Field"));
        this.clazz = v.clazz;
        this.name = v.name;
    }
    
    public String getClazz() {
        return clazz;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof FieldValue) {
            FieldValue f = ((FieldValue)o);
            return f.clazz.equals(clazz) && f.name.equals(name);
        }
        return false;
    }
}