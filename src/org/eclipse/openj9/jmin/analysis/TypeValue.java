package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;

public class TypeValue extends BasicValue {
    public TypeValue(Type type) {
        super(type);
    }
}