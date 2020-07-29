package org.eclipse.openj9.jmin.plugins;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM8;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class AnnotationProcessor extends ClassVisitor {
    private boolean matched;
    boolean trace;
    protected String[] classAnnotations;
    protected String[] constructorAnnotations;
    protected String[] methodAnnotations;
    protected String[] methodParameterAnnotations;
    protected String[] fieldAnnotations;
    protected String[] prefixes;
    protected WorkList worklist;
    protected String clazz;
    protected ReferenceInfo info;
    protected HierarchyContext context;
    public AnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(ASM8, next);
        this.worklist = worklist;
        this.classAnnotations = new String[0];
        this.constructorAnnotations = new String[0];
        this.methodAnnotations = new String[0];
        this.methodParameterAnnotations = new String[0];
        this.fieldAnnotations = new String[0];
        this.prefixes = new String[0];
        this.info = info;
        this.context = context;
    }  
    
    
    boolean matchesAnnotation(String desc, String[] annotations) {
        int start = -1;
        for (String prefix : prefixes) {
            if (desc.startsWith(prefix)) {
                start = prefix.length();
                break;
            }
        }
        if (prefixes.length == 0 || start > -1) {
            for (int i = 0; i < annotations.length; ++i) {
                if (desc.regionMatches(false, start, annotations[i], 0, annotations[i].length()) && desc.length() == start + annotations[i].length() + 1) {
                    matched = true;
                    return true;
                }
            }
        }
        return false;
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
        this.matched = false;
        this.trace = false;
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (matchesAnnotation(desc, classAnnotations)) {
            worklist.instantiateClass(clazz);
        }
        
        if (cv != null) {
            return cv.visitAnnotation(desc, visible);
        }
        return null;
    }
    
    @Override
    public FieldVisitor visitField(
        final int faccess,
        final String fname,
        final String fdesc,
        final String fsignature,
        final Object fvalue) {
        return new FieldVisitor(ASM8, cv != null ? cv.visitField(faccess, fname, fdesc, fsignature, fvalue) : null) {
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (matchesAnnotation(desc, fieldAnnotations)) {
                    worklist.instantiateClass(clazz);
                    worklist.processField(clazz, fname, fdesc);
                }
                if (fv != null) {
                    return fv.visitAnnotation(desc, visible);
                }
                return null;
            }
        };
    }
       
    @Override
    public MethodVisitor visitMethod(int maccess, java.lang.String mname, java.lang.String mdesc, java.lang.String msignature, java.lang.String[] mexceptions) {
        return new MethodVisitor(ASM8, cv != null ? cv.visitMethod(maccess, mname, mdesc, msignature, mexceptions) : null) {
            @Override
            public AnnotationVisitor visitAnnotation(java.lang.String desc, boolean visible) {
                if (matchesAnnotation(desc, methodAnnotations)) {
                    worklist.instantiateClass(clazz);
                    worklist.processVirtualMethod(clazz, mname, mdesc);
                }
                if (mv != null) {
                    return mv.visitAnnotation(desc, visible);
                }
                return null;
            }
            
            @Override
            public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
                if (matchesAnnotation(desc, methodParameterAnnotations)) {
                    worklist.instantiateClass(clazz);
                    worklist.processVirtualMethod(clazz, mname, mdesc);
                }
                if (mv != null) {
                    return mv.visitParameterAnnotation(parameter, desc, visible);
                }
                return null;
            }
        };
    }
    
    @Override
    public void visitEnd() {
        if (matched && trace) {
            System.out.println(this.getClass().getName() + " matched " + clazz);
        }
        if (cv != null) {
            cv.visitEnd();
        }
    }
}