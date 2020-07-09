package org.eclipse.openj9.jmin.writer;

import java.util.List;

import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class NonLoadingClassWriter extends ClassWriter {
    private HierarchyContext context;
    
    public NonLoadingClassWriter(HierarchyContext context, final int flags) {
        super(flags);
        this.context = context;
    }

    public NonLoadingClassWriter(HierarchyContext context, final ClassReader classReader, final int flags) {
        super(classReader, flags);
        this.context = context;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        type1 = type1.replace('.', '/');
        type2 = type2.replace('.', '/');
        List<String> supers1 = context.getSuperClasses(type1);
        List<String> supers2 = context.getSuperClasses(type2);
        if (supers1 == null || supers2 == null) {
            String msg = "";
            if (supers1 == null) {
                msg += " Unknown superclass for type1 " + type1;
            }
            if (supers2 == null) {
                msg += " Unknown superclass for type2 " + type2;
            }
            throw new RuntimeException(msg);
        }
        List<String> deeper = null;
        List<String> shallower = null;
        int deeperOffset = 0;
        if (supers1.size() > supers2.size()) {
            deeper = supers1;
            shallower = supers2;
            deeperOffset = deeper.size() - shallower.size();
            if (deeper.get(deeperOffset - 1).equals(type2)) {
                return type2;
            }
        } else if (supers1.size() < supers2.size()) {
            deeper = supers2;
            shallower = supers1;
            deeperOffset = deeper.size() - shallower.size();
            if (deeper.get(deeperOffset - 1).equals(type1)) {
                return type1;
            }
        } else {
            deeper = supers2;
            shallower = supers1;
            if (type1.equals(type2)) {
                return type1;
            }
        }
        
        String result = "java/lang/Object";
        for (int i = 0; i < shallower.size(); ++i) {
            if (deeper.get(i + deeperOffset).equals(shallower.get(i))) {
                result = shallower.get(i);
                break;
            }
        }
        return result;
    }
}