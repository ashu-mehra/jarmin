package org.eclipse.openj9.jmin.analysis;

import org.eclipse.openj9.jmin.info.CallKind;
import org.eclipse.openj9.jmin.info.ClassInfo;
import org.eclipse.openj9.jmin.info.FieldKind;
import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.ASM8;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;

import java.util.List;

public class ReferenceAnalyzer {
    public static ClassVisitor getReferenceInfoProcessor(String jar, ReferenceInfo info, HierarchyContext context) {
        return new ClassNode(ASM8) {
            private boolean isAnnotation;
            private String clazz;
            @Override
            public void visit(int version,
                  int access,
                  java.lang.String name,
                  java.lang.String signature,
                  java.lang.String superName,
                  java.lang.String[] interfaces) {
                this.clazz = name;
                isAnnotation = (access & Opcodes.ACC_ANNOTATION) != 0;
                context.addClassToJarMapping(clazz, jar);
                context.addSuperClass(clazz, superName);
                context.addInterfaces(clazz, interfaces);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
                AnnotationVisitor inner = super.visitAnnotation(descriptor, visible);
                if (isAnnotation && descriptor.equals("Ljava/lang/annotation/Retention;")) {
                    return new AnnotationVisitor(ASM8, inner) {
                        @Override
                        public void visitEnum(final String name, final String descriptor,
                                final String value) {
                            if (av != null) {
                                av.visitEnum(name, descriptor, value);
                            }
                            if (name.equals("value") && value.equals("RUNTIME")) {
                                context.addRuntimeAnnotation(clazz);
                            }
                        }
                    };
                }
                return inner;
            }

            @Override
            public void visitEnd() {
                ClassInfo cinfo = info.addClass(clazz);
                if (visibleAnnotations != null) {
                    for (AnnotationNode an : visibleAnnotations) {
                        cinfo.addAnnotation(an.desc.substring(1, an.desc.length() - 1));
                    }
                }
                for (FieldNode fn : fields) {
                    cinfo.addField(fn.name, fn.desc);
                    if (fn.visibleAnnotations != null) {
                        for (AnnotationNode an : fn.visibleAnnotations) {
                            cinfo.getField(fn.name, fn.desc).addAnnotation(an.desc.substring(1, an.desc.length() -1));
                        }
                    }
                }
                for (MethodNode mn : (List<MethodNode>) methods) {
                    try {
                        processMethod(clazz, mn, info, context);
                    } catch (AnalyzerException e) {

                    }
                    if (mn.visibleAnnotations != null) {
                        for (AnnotationNode an : mn.visibleAnnotations) {
                            cinfo.getMethod(mn.name, mn.desc).addAnnotation(an.desc.substring(1, an.desc.length() - 1));
                        }
                    }
                    if (mn.visibleParameterAnnotations != null) {
                        for (List<AnnotationNode> pan : mn.visibleParameterAnnotations) {
                            if (pan != null) {
                                for (AnnotationNode an : pan) {
                                    cinfo.getMethod(mn.name, mn.desc).addAnnotation(an.desc.substring(1, an.desc.length() -1));
                                }
                            }
                        }
                    }
                    
                }
                super.visitEnd();
            }
        };
    }
    private static void processMethod(String owner, MethodNode mn, ReferenceInfo info, HierarchyContext context) throws AnalyzerException {
        ClassInfo cinfo = info.addClass(owner);
        MethodInfo minfo = cinfo.addMethod(mn.name, mn.desc);
        for (Type t : Type.getArgumentTypes(mn.desc)) {
            if (t.getSort() == Type.OBJECT) {
                minfo.addReferencedClass(t.getInternalName());
            } else if (t.getSort() == Type.ARRAY) {
                do {
                    t = t.getElementType();
                } while (t.getSort() == Type.ARRAY);
                if (t.getSort() == Type.OBJECT) {
                    minfo.addReferencedClass(t.getInternalName());
                }
            }
        }
        {
            Type t = Type.getReturnType(mn.desc);
            if (t.getSort() == Type.OBJECT) {
                minfo.addReferencedClass(t.getInternalName());
            } else if (t.getSort() == Type.ARRAY) {
                do {
                    t = t.getElementType();
                } while (t.getSort() == Type.ARRAY);
                if (t.getSort() == Type.OBJECT) {
                    minfo.addReferencedClass(t.getInternalName());
                }
            }
        }
        AnalysisFrames analysisFrames = new AnalysisFrames(owner, mn);
        AbstractInsnNode[] insns = mn.instructions.toArray();
        for (int i = 0; i < insns.length; ++i) {
            AbstractInsnNode insn = insns[i];
            if (true) {
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode)insn;
                        
                    if (insn.getOpcode() == INVOKESTATIC) {
                        if (m.owner.equals("java/lang/Class")
                            && m.name.equals("forName")
                            && m.desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")) {
                            BasicValue arg = analysisFrames.getStackValue(i, 0);
                            if (arg != null && arg instanceof StringValue && ((StringValue)arg).getContents() != null) {
                                String clazz = ((StringValue)arg).getContents();
                                //System.out.println("reflected class " + clazz);
                                minfo.addReferencedClass(clazz);
                                minfo.addInstantiatedClass(clazz);
                            } else {
                                //System.out.println("! unknown Class.forName at " + owner + " " + mn.name + " " + mn.desc);
                            }
                        } else if (m.owner.equals("java/util/ServiceLoader")
                            && m.name.equals("load")) {
                            BasicValue arg = analysisFrames.getStackValue(i, 0);
                            // TODO this is not very precise right now so we just load everything if we can't find the service to load
                            // need interprocedural analysis to find the class names that are arriving

                            if (arg != null && arg instanceof ClassValue && context.getServiceProviders(((ClassValue)arg).getName()) != null) {
                                for (String svc : context.getServiceProviders(((ClassValue)arg).getName())) {
                                    minfo.addReferencedClass(svc);
                                    minfo.addInstantiatedClass(svc);
                                }
                            } else {
                                for (String si : context.getServiceInterfaces()) {
                                    minfo.addReferencedClass(si);
                                    for (String svc : context.getServiceProviders(si)) {
                                        minfo.addReferencedClass(svc);
                                        minfo.addInstantiatedClass(svc);
                                    }
                                }
                            }
                        }
                        minfo.addCallSite(m.owner, m.name, m.desc, CallKind.STATIC);
                    } else if (insn.getOpcode() == INVOKEVIRTUAL) {
                        if (m.owner.equals("java/lang/reflect/Method")
                            && m.name.equals("invoke")
                            && m.desc.equals("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;")) {
                            BasicValue arg = analysisFrames.getStackValue(i, 2);
                            if (arg != null && arg instanceof MethodValue) {
                                MethodValue mv = (MethodValue)arg;
                                String mclazz = mv.getClazz();
                                minfo.addCallSite(mclazz, mv.getName(), "*", CallKind.VIRTUAL);
                            }
                        }
                        else if (m.owner.equals("java/lang/Class")
                            && m.name.equals("newInstance")
                            && m.desc.equals("()Ljava/lang/Object;")) {
                            BasicValue arg = analysisFrames.getStackValue(i, 0);
                            if (arg != null && arg instanceof ClassValue) {
                                String init = ((ClassValue)arg).getName();
                                minfo.addInstantiatedClass(init);
                            } else {
                                //System.out.println("! Unknown Class.newInstance - type information may be incomplete");
                            }
                        }
                        minfo.addCallSite(m.owner, m.name, m.desc, CallKind.VIRTUAL);
                    } else if (insn.getOpcode() == INVOKEINTERFACE) {
                        if (m.owner.equals("org/hibernate/boot/registry/classloading/spi/ClassLoaderService")
                            && m.name.equals("classForName")
                            && m.desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")) {
                            BasicValue arg = analysisFrames.getStackValue(i, 0);
                            if (arg != null && arg instanceof StringValue && ((StringValue)arg).getContents() != null) {
                                String clazz = ((StringValue)arg).getContents();
                                minfo.addReferencedClass(clazz);
                            }
                        }
                        minfo.addCallSite(m.owner, m.name, m.desc, CallKind.INTERFACE);
                    } else {
                        minfo.addCallSite(m.owner, m.name, m.desc, CallKind.SPECIAL);
                    }
                    /*if (m.owner.startsWith("java/awt") || m.owner.startsWith("javax/swing")) {
                        System.out.println("@^@ " + m.owner + " from typeinsnnode in " + owner + "." + mn.name + mn.desc);
                    }*/
                } else if (insn instanceof TypeInsnNode) {
                    TypeInsnNode t = (TypeInsnNode)insn;
                    /*if (t.desc.startsWith("java/awt") || t.desc.startsWith("javax/swing")) {
                        System.out.println("@^@ " + t.desc + " from typeinsnnode in " + owner + "." + mn.name + mn.desc);
                    }*/
                    minfo.addReferencedClass(t.desc);
                    if (t.getOpcode() == Opcodes.NEW) {
                        minfo.addInstantiatedClass(t.desc);
                    }
                } else if (insn instanceof FieldInsnNode) {
                    FieldInsnNode f = (FieldInsnNode)insn;
                    minfo.addReferencedField(f.owner, f.name, f.desc, f.getOpcode() == Opcodes.GETSTATIC || f.getOpcode() == Opcodes.PUTSTATIC ? FieldKind.STATIC : FieldKind.INSTANCE);
                } else if (insn instanceof LdcInsnNode) {
                    LdcInsnNode l = (LdcInsnNode)insn;
                    if (l.cst instanceof Type) {
                        Type t = (Type)l.cst;
                        if (t.getSort() == OBJECT) {
                            /*if (t.getInternalName().startsWith("java/awt") || t.getInternalName().startsWith("javax/swing")) {
                                    System.out.println("@^@ " + t.getInternalName() + " from array ldc in " + owner + "." + mn.) {
                                System.out.println("@^@ " + t.getInternalName() + " from class ldc in " + owner + "." + mn.name + mn.desc);
                            }*/
                            minfo.addReferencedClass(t.getInternalName());
                        } else if (t.getSort() == ARRAY) {
                            do {
                                t = t.getElementType();
                            } while (t.getSort() == ARRAY);
                            if (t.getSort() == OBJECT) {
                                /*if (t.getInternalName().startsWith("java/awt") || t.getInternalName().startsWith("javax/swing")) {
                                    System.out.println("@^@ " + t.getInternalName() + " from array ldc in " + owner + "." + mn.name + mn.desc);
                                }*/
                                minfo.addReferencedClass(t.getInternalName());
                            }
                        }
                    }
                } else if (insn instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode)insn;
                    Handle h = indy.bsm;
                    if (h.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                        && (h.getName().equals("metafactory") || h.getName().equals("altMetafactory"))
                        && indy.bsmArgs.length > 2
                        && indy.bsmArgs[1] instanceof Handle) {
                        Handle hBSM = (Handle)indy.bsmArgs[1];
                        if (hBSM.getName().equals("<init>")) {
                            minfo.addReferencedClass(hBSM.getOwner());
                            minfo.addInstantiatedClass(hBSM.getOwner());
                        }
                        minfo.addCallSite(hBSM.getOwner(), hBSM.getName(), hBSM.getDesc(), CallKind.DYNAMIC);
                    } else {
                        System.out.println("indy name " + indy.name + " desc " + indy.desc);
                        System.out.println("  handle " + h.getOwner() + " " + h.getName() + " " + h.getDesc());
                        for (Object o : indy.bsmArgs) {
                            System.out.println("  " + o.getClass().getName() + ":" + o.toString());
                        }
                        //throw new RuntimeException("Unknown indy");
                    }
                }
            }
        }
    }
}

class AnalysisFrames {
    private Frame<BasicValue>[] frames;
    private String owner;
    private MethodNode mn;
    public AnalysisFrames(String owner, MethodNode mn) {
        this.owner = owner;
        this.mn = mn;
    }
    public BasicValue getStackValue(int instructionIndex, int frameIndex) throws AnalyzerException {
        if (frames == null) {
            Analyzer<BasicValue> a = new Analyzer<BasicValue>(new ReflectionInterpreter());
            a.analyze(owner, mn);
            frames = a.getFrames();
        }
        Frame<BasicValue> f = frames[instructionIndex];
        if (f == null) {
            return null;
        }
        int top = f.getStackSize() - 1;
        return frameIndex <= top ? f.getStack(top - frameIndex) : null;
    }
}