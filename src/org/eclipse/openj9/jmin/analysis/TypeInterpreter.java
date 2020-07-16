package org.eclipse.openj9.jmin.analysis;

import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

public class TypeInterpreter extends BasicInterpreter {
    private HierarchyContext context;
    public TypeInterpreter(HierarchyContext context) {
        super(ASM8);
        this.context = context;
    }

    @Override
    public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        if (insn.getOpcode() == Opcodes.NEW) {
            return new FixedTypeValue(Type.getObjectType(((TypeInsnNode) insn).desc));
        }
        return super.newOperation(insn);
    }

    @Override
    public BasicValue newValue(final Type type) {
        if (type != null
            && type.getSort() == Type.OBJECT
            && type.getDescriptor() != null) {
            return new TypeValue(type);
        }
        return super.newValue(type);
    }

    @Override
    public BasicValue merge(BasicValue v1, BasicValue v2) {
        if (v1 instanceof TypeValue && v2 instanceof TypeValue) {
            String desc1 = v1.getType().getDescriptor(), desc2 = v2.getType().getDescriptor();

            if (desc1.equals(desc2) || context.getSuperClasses(desc2).contains(desc1)) {
                return v1;
            } else if (context.getSuperClasses(desc1).contains(desc2)) {
                return v2;
            }
        } else if (v1 instanceof FixedTypeValue && v2 instanceof FixedTypeValue) {
            String desc1 = v1.getType().getDescriptor(), desc2 = v2.getType().getDescriptor();
            if (desc1.equals(desc2)){
                return v1;
            } else if (context.getSuperClasses(desc2).contains(desc1)) {
                return new TypeValue(v1.getType());
            } else if (context.getSuperClasses(desc1).contains(desc2)) {
                return new TypeValue(v2.getType());
            }
        } else if (v1 instanceof FixedTypeValue && v2 instanceof TypeValue) {
            String desc1 = v1.getType().getDescriptor(), desc2 = v2.getType().getDescriptor();
            if (desc1.equals(desc2)) {
                return v2;
            } else if (context.getSuperClasses(desc1).contains(desc2)) {
                return v2;
            } else if (context.getSuperClasses(desc2).contains(desc1)) {
                return new TypeValue(v1.getType());
            }
        } else if (v1 instanceof TypeValue && v2 instanceof FixedTypeValue) {
            String desc1 = v1.getType().getDescriptor(), desc2 = v2.getType().getDescriptor();
            if (desc1.equals(desc2)) {
                return v1;
            } else if (context.getSuperClasses(desc1).contains(desc2)) {
                return new TypeValue(v2.getType());
            } else if (context.getSuperClasses(desc2).contains(desc1)) {
                return v1;
            }
        }
        return super.merge(degradeValue(v1), degradeValue(v2));
    }

    private BasicValue degradeValue(BasicValue v) {
        if (v instanceof TypeValue || v instanceof FixedTypeValue) {
            return BasicValue.REFERENCE_VALUE;
        }
        return v;
    }
}