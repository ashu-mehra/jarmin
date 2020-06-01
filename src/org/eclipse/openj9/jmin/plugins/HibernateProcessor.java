package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM8;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;;

public class HibernateProcessor extends ClassVisitor {
    private String clazz;
    private WorkList worklist;
    boolean isSPIInterface;
    public HibernateProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(ASM8, next);
        this.worklist = worklist;
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
        this.isSPIInterface = false;
        if ((access & ACC_INTERFACE) != 0
            && name.startsWith("org/hibernate")
            && name.contains("/spi/")) {
                isSPIInterface = true;
            }
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
        
    }
    
    @Override
    public MethodVisitor visitMethod(int access, java.lang.String name, java.lang.String desc, java.lang.String signature, java.lang.String[] exceptions) {
        if (isSPIInterface && ((access & ACC_PUBLIC) != 0)) {
            worklist.processInterfaceMethod(clazz, name, desc);
        }
        if (cv != null) {
          return cv.visitMethod(access, name, desc, signature, exceptions);
        }
        return null;
    }
}