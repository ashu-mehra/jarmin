package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM8;

public class JBossLoggingAnnotationProcessor extends ClassVisitor {
    private String clazz;
    private WorkList worklist;
    private HierarchyContext context;
    public JBossLoggingAnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(ASM8, next);
        this.worklist = worklist;
        this.context = context;
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
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
        
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (desc.startsWith("Lorg/jboss/logging/annotations/")) {
            String search = clazz + "_$";
            for (String c : context.seenClasses()) {
                if (c.startsWith(search)) {
                    worklist.forceInstantiateClass(c);
                }
            }
        }
        
        if (cv != null) {
            return cv.visitAnnotation(desc, visible);
        }
        return null;
    }
    
    @Override
    public MethodVisitor visitMethod(int maccess, java.lang.String mname, java.lang.String mdesc, java.lang.String msignature, java.lang.String[] mexceptions) {
        return new MethodVisitor(ASM8, cv != null ? cv.visitMethod(maccess, mname, mdesc, msignature, mexceptions) : null) {
            @Override
            public AnnotationVisitor visitAnnotation(java.lang.String desc, boolean visible) {
                if (desc.startsWith("Lorg/jboss/logging/annotations/")) {
                    worklist.processMethod(clazz, mname, mdesc);
                }
                if (mv != null) {
                    return mv.visitAnnotation(desc, visible);
                }
                return null;
            }
        };
    }
}