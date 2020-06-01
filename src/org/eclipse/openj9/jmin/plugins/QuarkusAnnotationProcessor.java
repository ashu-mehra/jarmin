package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM8;

public class QuarkusAnnotationProcessor extends ClassVisitor {
    private String clazz;
    private boolean isRecorder;
    private WorkList worklist;
    private HierarchyContext context;
    public QuarkusAnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
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
        this.isRecorder = false;
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
        
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (desc.equals("Lio/quarkus/runtime/annotations/Recorder;")) {
            isRecorder = true;
            worklist.processClass(clazz);
        }
        
        if (cv != null) {
            return cv.visitAnnotation(desc, visible);
        }
        return null;
    }
    
    @Override
    public MethodVisitor visitMethod(int maccess, java.lang.String mname, java.lang.String mdesc, java.lang.String msignature, java.lang.String[] mexceptions) {
        if (isRecorder) {
            worklist.processMethod(clazz, mname, mdesc);
        } else if (mname.equals("created") && mdesc.equals("(Lio/quarkus/arc/runtime/BeanContainer;)V")
                    && context.getInterfaceImplementors("io/quarkus/arc/runtime/BeanContainerListener").contains(clazz)) {
            worklist.processVirtualMethod(clazz, mname, mdesc);
        }
        return new MethodVisitor(ASM8, cv != null ? cv.visitMethod(maccess, mname, mdesc, msignature, mexceptions) : null) {
            @Override
            public AnnotationVisitor visitAnnotation(java.lang.String desc, boolean visible) {
                if (desc.equals("Lio/quarkus/deployment/annotations/BuildStep;")) {
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