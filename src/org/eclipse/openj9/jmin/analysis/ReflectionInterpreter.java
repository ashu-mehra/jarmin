package org.eclipse.openj9.jmin.analysis;

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import static org.objectweb.asm.tree.analysis.BasicValue.REFERENCE_VALUE;

public class ReflectionInterpreter extends BasicInterpreter {
    public ReflectionInterpreter() {
        super(ASM8);
    }
    
    @Override
    public BasicValue newOperation(AbstractInsnNode insn) throws AnalyzerException 
    {
      if (insn instanceof LdcInsnNode) {
          LdcInsnNode ldc = (LdcInsnNode)insn;
          Object cst = ldc.cst;
          if (cst instanceof String) {
              return new StringValue((String)cst);
          } else if (cst instanceof Type) {
              return new ClassValue(((Type)cst).getInternalName());
          }
      } else if (insn instanceof TypeInsnNode && insn.getOpcode() == NEW) {
          TypeInsnNode n = (TypeInsnNode)insn;
          if (n.desc.equals("java/lang/String")) {
              return new StringValue(); // contents will be set later (potentially)
          } else if (n.desc.equals("java/lang/StringBuilder")) {
              return new StringBuilderValue(); // appender will create new values later
          }
      }
      return super.newOperation(insn);
    }
    
    @Override
    public BasicValue naryOperation(AbstractInsnNode insn, List<? extends BasicValue> values) throws AnalyzerException {
        return this.naryOperation(insn, values, null);
    }
    public BasicValue naryOperation(AbstractInsnNode insn, List<? extends BasicValue> values, final InterospectiveFrame frame) throws AnalyzerException {
      if (insn instanceof MethodInsnNode) {
          MethodInsnNode m = (MethodInsnNode) insn;
          if (m.getOpcode() == INVOKESPECIAL) {
              // handle string constructors
              if (m.owner.equals("java/lang/String")
                  && m.name.equals("<init>")) {
                  if (m.desc.equals("()V")
                      && values.get(0) instanceof StringValue) {
                      ((StringValue)values.get(0)).setContents("");
                  } else if (m.desc.equals("(Ljava/lang/String;)V")
                      && values.get(0) instanceof StringValue 
                      && values.get(1) instanceof StringValue) {
                      ((StringValue)values.get(0)).setContents(((StringValue)values.get(1)).getContents());
                  }
              }
          } else if (m.getOpcode() == INVOKESTATIC) {
              if (m.owner.equals("java/lang/Class")
                  && m.name.equals("forName")
                  && m.desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")
                  && values.get(0) instanceof StringValue
                  && ((StringValue)values.get(0)).getContents() != null) {
                  return new ClassValue(((StringValue)values.get(0)).getContents());
              }
          } else if (m.getOpcode() == INVOKEVIRTUAL) {
              if (m.owner.equals("java/lang/Class")) {
                    if ((m.name.equals("getMethod") || m.name.equals("getDeclaredMethod"))
                        && m.desc.equals("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")
                        && values.get(0) instanceof ClassValue
                        && values.get(1) instanceof StringValue
                        && ((StringValue)values.get(1)).getContents() != null) {
                        return new MethodValue(((ClassValue)values.get(0)).getName(), ((StringValue)values.get(1)).getContents(), "*");
                    } else if ((m.name.equals("getField") || m.name.equals("getDeclaredField"))
                               && m.desc.equals("(Ljava/lang/String;)Ljava/lang/reflect/Field;")
                               && values.get(0) instanceof ClassValue
                               && values.get(1) instanceof StringValue
                               && ((StringValue)values.get(1)).getContents() != null) {
                        return new FieldValue(((ClassValue)values.get(0)).getName(), ((StringValue)values.get(1)).getContents());
                    }
              } else if (m.owner.equals("java/lang/StringBuilder")) {
                    if (m.name.equals("append")) {
                        if (m.desc.equals("(Ljava/lang/String;)Ljava/lang/StringBuilder;")
                            && values.get(0) != null
                            && values.get(0) instanceof StringBuilderValue
                            && values.get(1) != null 
                            && values.get(1) instanceof StringValue) {
                            StringBuilderValue sbv = new StringBuilderValue((StringBuilderValue)values.get(0));
                            sbv.append(values.get(1));
                            if (frame != null) {
                                frame.replaceValue(values.get(0), sbv);
                            }
                            return sbv;
                        }
                    } else if (m.name.equals("toString") && m.desc.equals("()Ljava/lang/String;")) {
                        
                        if (values.get(0) instanceof StringBuilderValue) {
                            return ((StringBuilderValue)values.get(0)).getContents();
                        }
                    }
              }
          }
      }
      return super.naryOperation(insn, values);
    }
  
    @Override
    public BasicValue newParameterValue(final boolean isInstanceMethod, final int local, final Type type) {
        return new ParameterValue(local, newValue(type));
    }

    @Override
    public BasicValue merge(BasicValue v1, BasicValue v2) {
        if (v1 instanceof StringValue 
            && v2 instanceof StringValue
            && v1.equals(v2)) {
            return new StringValue((StringValue)v1);
        } else if (v1 instanceof ClassValue
                   && v2 instanceof ClassValue
                   && v1.equals(v2)) {
            return new ClassValue((ClassValue)v1);
        } else if (v1 instanceof MethodValue
                   && v2 instanceof MethodValue
                   && v1.equals(v2)) {
            return new MethodValue((MethodValue)v2);
        } else if (v1 instanceof StringBuilderValue
                   && v2 instanceof StringBuilderValue
                   && v1.equals(v2)) {
            return new StringBuilderValue((StringBuilderValue)v1);
        } else if (v1 instanceof ParameterValue
                   && v2 instanceof ParameterValue) {
            if (((ParameterValue)v1).equals(v2)) {
                return new ParameterValue((ParameterValue)v2);
            }
            return this.merge(((ParameterValue)v1).getValue(), ((ParameterValue)v2).getValue());
        }
        return super.merge(degradeValue(v1), degradeValue(v2));
    }
  
    private BasicValue degradeValue(BasicValue v) {
        if (v instanceof StringValue || v instanceof ClassValue || v instanceof MethodValue || v instanceof StringBuilderValue) {
            return REFERENCE_VALUE;
        }
        if (v instanceof ParameterValue) {
            return degradeValue(((ParameterValue)v).getValue());
        }
        return v;
    }
}