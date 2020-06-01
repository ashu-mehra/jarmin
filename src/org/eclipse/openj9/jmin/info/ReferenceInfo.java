package org.eclipse.openj9.jmin.info;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.openj9.jmin.util.HierarchyContext;

public class ReferenceInfo {
    private final HashMap<String, ClassInfo> classInfo;
    public ReferenceInfo() {
        classInfo = new HashMap<String, ClassInfo>();
    }
    public ClassInfo addClass(String name) {
        if (!classInfo.containsKey(name)) {
            classInfo.put(name, new ClassInfo(name));
        }
        return classInfo.get(name);
    }
    public boolean hasClass(String name) {
        return classInfo.containsKey(name);
    }
    public boolean isClassReferenced(String name) {
        ClassInfo info = classInfo.get(name);
        return info != null && info.isReferenced();
    }
    public ClassInfo getClassInfo(String name) {
        return classInfo.get(name);
    }
    public int getReferencedClassCount() {
        int toReturn = 0;
        for (String c : classInfo.keySet()) {
            if (classInfo.get(c).isReferenced()) {
                toReturn ++;
            }
        }
        return toReturn;
    }
    public int getClassCount() {
        return classInfo.size();
    }
    public String markFieldReference(HierarchyContext context, String clazz, String name, String desc) {
        ClassInfo ci = classInfo.get(clazz);
        String foundClazz = null;
        if (ci != null && ci.hasField(name, desc)) {
            foundClazz = clazz;
            ci.markFieldReferenced(name, desc);
        } else {
            for (String superClass : context.getSuperClasses(clazz)) {
                ci = classInfo.get(superClass);
                if (ci != null && ci.hasField(name, desc)) {
                    foundClazz = superClass;
                    ci.markFieldReferenced(name, desc);
                    break;
                }
            }
        }
        return foundClazz;
    }
    private ClassInfo findClassOfMethod(HierarchyContext context, String clazz, String name, String desc) {
        ClassInfo ci = classInfo.get(clazz);
        ClassInfo foundClazz = null;
        if (ci != null && ci.hasMethod(name, desc)) {
            foundClazz = ci;
        } else {
            LinkedList<String> classesToCheck = new LinkedList<String>();
            classesToCheck.add(clazz);
            classesToCheck.addAll(context.getSuperClasses(clazz));
            
            while (!classesToCheck.isEmpty()) {
                String c = classesToCheck.pop();
                ci = classInfo.get(c);
                if (ci != null && ci.hasMethod(name, desc)) {
                    foundClazz = ci;
                    break;
                }
                String[] interfaces = context.getClassInterfaces(c);
                for (int i = 0; i < interfaces.length; ++i) {
                    classesToCheck.add(interfaces[i]);
                }
            }
        }
        return foundClazz;
    }
    public String findDeclaringClassOfMethod(HierarchyContext context, String clazz, String name, String desc) {
        ClassInfo ci = findClassOfMethod(context, clazz, name, desc);
        return ci == null ? null : ci.name();
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String clazz : classInfo.keySet()) {
            sb.append(classInfo.get(clazz).toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}