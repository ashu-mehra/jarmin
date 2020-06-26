package org.eclipse.openj9.jmin.info;

import org.objectweb.asm.tree.analysis.BasicValue;

public class CallSite {
    public final MethodInfo caller;
    public final String clazz;
    public final String name;
    public final String desc;
    public final CallKind kind;
    public final int instructionIndex;
    private BasicValue[] argValueList;
    public CallSite(MethodInfo caller, String clazz, String name, String desc, CallKind kind, int instructionIndex) {
        //if (clazz.equals("com/arjuna/ats/internal/jta/Implementationsx"))
        //throw new RuntimeException();
        this.caller = caller;
        this.clazz = new String(clazz.toCharArray());
        this.name = new String(name.toCharArray());
        this.desc = new String(desc.toCharArray());
        this.kind = kind;
        this.instructionIndex = instructionIndex;
        this.argValueList = null;
    }

    public int getInstructionIndex() { return instructionIndex; }

    public MethodInfo getCaller() {
        return caller;
    }

    public void setArgValueList(BasicValue[] argValueList) {
        this.argValueList = argValueList;
    }

    public BasicValue getArgValue(int argIndex) {
        return argValueList[argIndex];
    }

    public BasicValue[] getAllArgValues() {
        return argValueList;
    }

    @Override
    public String toString() {
        return "CallSite " + kind.toString() + ": " + clazz + "." + name + desc;
    }
}