package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;

public class FixedTypeValue extends BasicValue {
    public FixedTypeValue(Type type) {
        super(type);
    }
}