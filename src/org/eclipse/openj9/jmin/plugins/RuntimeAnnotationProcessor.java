package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.ASM8;

public class RuntimeAnnotationProcessor extends ClassVisitor {
    private WorkList worklist;
    private HierarchyContext context;

    public RuntimeAnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
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
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        String annotClazz = descriptor.substring(1, descriptor.length() - 2);
        if (context.hasRuntimeAnnotation(annotClazz)) {
            worklist.instantiateClass(annotClazz);
        }
        if (cv != null) {
            return cv.visitAnnotation(descriptor, visible);
        }
        return null;
    }
    
    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        String annotClazz = descriptor.substring(1, descriptor.length() - 1);
        if (context.hasRuntimeAnnotation(annotClazz)) {
            worklist.instantiateClass(annotClazz);
        }
        if (cv != null) {
        return cv.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }
        return null;
    }
    
    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
        return new FieldVisitor(ASM8, cv != null ? cv.visitField(access, name, descriptor, signature, value) : null) {
            @Override
            public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (fv != null) {
                    return fv.visitAnnotation(descriptor, visible);
                }
                return null;
            }
            @Override
            public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (fv != null) {
                    return fv.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
                }
                return null;
            }
        };
    }
    
    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
        return new MethodVisitor(ASM8, cv != null ? cv.visitMethod(access, name, descriptor, signature, exceptions) : null) {
            @Override
            public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (mv != null) {
                    return mv.visitAnnotation(descriptor, visible);
                }
                return null;
            }
            
            @Override
            public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (mv != null) {
                  return mv.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
                }
                return null;
            }
            
            @Override
            public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (mv != null) {
                    return mv.visitParameterAnnotation(parameter, descriptor, visible);
                }
                return null;
            }
            
            @Override
            public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (mv != null) {
                    return mv.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
                }
                return null;
            }
            
            @Override
            public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (mv != null) {
                    return mv.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
                }
                return null;
            }
            
            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(
                final int typeRef,
                final TypePath typePath,
                final Label[] start,
                final Label[] end,
                final int[] index,
                final String descriptor,
                final boolean visible) {
                String annotClazz = descriptor.substring(1, descriptor.length() - 1);
                if (context.hasRuntimeAnnotation(annotClazz)) {
                    worklist.instantiateClass(annotClazz);
                }
                if (mv != null) {
                return mv.visitLocalVariableAnnotation(
                    typeRef, typePath, start, end, index, descriptor, visible);
                }
                return null;
            }
        };
    }
}