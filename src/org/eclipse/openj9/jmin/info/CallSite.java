package org.eclipse.openj9.jmin.info;

public class CallSite {
    public final String clazz;
    public final String name;
    public final String desc;
    public final CallKind kind;
    public CallSite(String clazz, String name, String desc, CallKind kind) {
        //if (clazz.equals("com/arjuna/ats/internal/jta/Implementationsx"))
        //throw new RuntimeException();
        this.clazz = new String(clazz.toCharArray());
        this.name = new String(name.toCharArray());
        this.desc = new String(desc.toCharArray());
        this.kind = kind;
    }
    @Override
    public String toString() {
        return "CallSite " + kind.toString() + ": " + clazz + "." + name + desc;
    }
}