package org.eclipse.openj9.jmin.analysis;

import org.objectweb.asm.tree.analysis.BasicValue;

public class ParameterValue extends BasicValue {
    private BasicValue bv;
    private int index;
    public ParameterValue(int index, BasicValue bv) {
        super(bv.getType());
        this.bv = bv;
        this.index = index;
    }
    public ParameterValue(ParameterValue v) {
        super(v.getType());
        this.index = v.index;
        this.bv = v.bv;
    }
    public int getIndex() {
        return index;
    }
    public BasicValue getValue() {
        return bv;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof ParameterValue) {
            return index == ((ParameterValue)o).index && this.bv.equals(((ParameterValue)o).bv);
        }
        return false;
    }
}