package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM8;

public class MBeanProcessor extends ClassVisitor {
    private static final String MBEAN_SUFFIX = "MBean";
    private static final String MXBEAN_SUFFIX = "MXBean";
    private static final String[] suffixes = new String[] { MBEAN_SUFFIX, MXBEAN_SUFFIX };
    private String clazz;
    private WorkList worklist;
    private HierarchyContext context;
    private ReferenceInfo info;
    private boolean trace;
    private boolean matched;
    public MBeanProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(ASM8, next);
        this.worklist = worklist;
        this.context = context;
        this.info = info;
    }
    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) {
        this.clazz = name;
        this.trace = false;
        this.matched = false;
        for (String s : suffixes) {
            if (name.endsWith(s)) {
                matched = true;
                for (MethodInfo mi : info.getClassInfo(name).getMethodsByNameOnly("<init>")) {
                    worklist.processMethod(name, "<init>", mi.desc());
                }
                for (String i : context.getInterfaceImplementors(name)) {
                    for (MethodInfo mi : info.getClassInfo(i).getMethodsByNameOnly("<init>")) {
                        worklist.processMethod(i, "<init>", mi.desc());
                    }
                }
                break;
            }
        }
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
        
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
        if (matched) {
            worklist.processMethod(clazz, name, descriptor);
        }
        if (cv != null) {
            return cv.visitMethod(access, name, descriptor, signature, exceptions);
        }
        return null;
    }
    
    @Override
    public void visitEnd() {
        if (matched && trace) {
            System.out.println("@ " + this.getClass().getName() + " matched " + clazz);
        }
        if (cv != null) {
            cv.visitEnd();
        }
    }
}