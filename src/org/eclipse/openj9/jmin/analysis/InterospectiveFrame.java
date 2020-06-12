package org.eclipse.openj9.jmin.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import jdk.internal.org.objectweb.asm.Opcodes;

public class InterospectiveFrame extends Frame<BasicValue> {
    public InterospectiveFrame(final int nlocals, final int nstack) {
        super(nlocals, nstack);
    }
    public InterospectiveFrame(final Frame<? extends BasicValue> src) {
        super(src);
    }
    public void replaceValue(BasicValue orig, BasicValue next) {
        Stack<BasicValue> mirror = new Stack<BasicValue>();
        for (int i = 0, e = getStackSize(); i < e; ++i) {
            BasicValue v = pop();
            mirror.push(v == orig ? next : v);
        }
        for (int i = 0, e = mirror.size(); i < e; ++i) {
            push(mirror.pop());
        }
        for (int i = 0, e = getLocals(); i < e; ++i) {
            BasicValue local = getLocal(i);
            if (local == orig) {
                setLocal(i, next);
            }
        }
    }
    @Override
    public void execute(final AbstractInsnNode insn, final Interpreter<BasicValue> interpreter) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                List<BasicValue> values = new ArrayList<BasicValue>();
                String desc = ((MethodInsnNode) insn).desc;
                for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
                    values.add(0, pop());
                }
                if (insn.getOpcode() != Opcodes.INVOKESTATIC) {
                    values.add(0, pop());
                }
                if (Type.getReturnType(desc) == Type.VOID_TYPE) {
                    if (interpreter instanceof ReflectionInterpreter) {
                        ((ReflectionInterpreter)interpreter).naryOperation(insn, values, this);
                    } else {
                        interpreter.naryOperation(insn, values);
                    }
                } else {
                    if (interpreter instanceof ReflectionInterpreter) {
                        push(((ReflectionInterpreter)interpreter).naryOperation(insn, values, this));
                    } else {
                        push(interpreter.naryOperation(insn, values));
                    }
                }
                return;
            }
        }
        super.execute(insn, interpreter);
    }
}