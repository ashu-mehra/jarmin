package org.eclipse.openj9.jmin.util;

import java.util.LinkedList;

import org.eclipse.openj9.jmin.WorkItem;
import org.eclipse.openj9.jmin.info.ClassInfo;
import org.eclipse.openj9.jmin.info.FieldInfo;
import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;

public class WorkList {
    private HierarchyContext context;
    private ReferenceInfo info;
    private LinkedList<WorkItem> worklist;

    public WorkList(ReferenceInfo info, HierarchyContext context) {
        this.worklist = new LinkedList<WorkItem>();
        this.context = context;
        this.info = info;
    }

    public boolean hasNext() {
        return worklist.size() > 0;
    }

    public WorkItem next() {
        return worklist.removeFirst();
    }

    private void handleClass(String c) {
        if (!info.isClassReferenced(c)) {
            info.addClass(c).setReferenced();
            if (info.getClassInfo(c).hasMethod("<clinit>", "()V") 
                && !info.getClassInfo(c).isMethodReferenced("<clinit>", "()V")) {
                worklist.add(new WorkItem(c, "<clinit>", "()V"));
            }
            ClassInfo cinfo = info.getClassInfo(c);
            for (String annot : cinfo.getAnnotations()) {
                processClass(annot);
            }
            for (FieldInfo field : cinfo.getFields()) {
                if (!field.referenced()) {
                    field.setReferenced();
                    String desc = field.desc();
                    if (desc.charAt(0) == 'L' && desc.charAt(desc.length() - 1) == ';') {
                        String fclazz = desc.substring(1, desc.length() - 1);
                        processClass(fclazz);
                    }
                }
            }
            LinkedList<String> classesToCheck = new LinkedList<String>();
            classesToCheck.add(c);
            classesToCheck.addAll(context.getSuperClasses(c));
            
            while (!classesToCheck.isEmpty()) {
                String check = classesToCheck.pop();
                String[] interfaces = context.getClassInterfaces(check);
                for (int i = 0; i < interfaces.length; ++i) {
                    classesToCheck.add(interfaces[i]);
                }
                if (info.getClassInfo(check) != null) {
                    for (MethodInfo mi : info.getClassInfo(check).getReferencedMethods()) {
                        if (cinfo.hasMethod(mi.name(), mi.desc())
                            && !cinfo.isMethodReferenced(mi.name(), mi.desc())) {
                            worklist.add(new WorkItem(c, mi.name(), mi.desc()));
                        }
                    }
                }
            }
        }
    }

    public void processClass(String clazz) {
        if (clazz.indexOf('.') != -1) {
            clazz = clazz.replace('.', '/');
            //throw new RuntimeException("bad class name" + clazz);
        }
        handleClass(clazz);
        for (String i : context.getClassInterfaces(clazz)) {
            processClass(i);
        }
        for (String c : context.getSuperClasses(clazz)) {
            processClass(c);
            for (String i : context.getClassInterfaces(c)) {
                processClass(i);
            }
        }
    }

    public void processField(String clazz, String name, String desc) {
        processClass(clazz);
        String foundClazz = info.markFieldReference(context, clazz, name, desc);
        if (foundClazz == null) {
            return;//throw new RuntimeException("Could not find field " + clazz + "." + name + desc);
        }
        for (String annot : info.addClass(foundClazz).getField(name, desc).getAnnotations()) {
            processClass(annot);
        }
    }

    public void processMethod(String clazz, String name, String desc) {
        processClass(clazz);
        clazz = info.findDeclaringClassOfMethod(context, clazz, name, desc);
        if (clazz != null && !info.getClassInfo(clazz).isMethodReferenced(name, desc)) {
            info.getClassInfo(clazz).markMethodReferenced(name, desc);
            worklist.add(new WorkItem(clazz, name, desc));
            for (String annot : info.getClassInfo(clazz).getMethod(name, desc).getAnnotations()) {
                processClass(annot);
            }
            if (context.getServiceProviders(clazz) != null) {
                for (String c : context.getServiceProviders(clazz)) {
                    processMethod(c, name, desc);
                }
            }
        }      
    }

    public void processVirtualMethod(String clazz, String name, String desc) {
        processMethod(clazz, name, desc);
        if (context.getSubClasses(clazz) != null) {
            for (String c : context.getSubClasses(clazz)) {
                if (info.isClassReferenced(c)) {
                    processMethod(c, name, desc);
                }
            }
        }
    }

    public void processInterfaceMethod(String clazz, String name, String desc) {
        processMethod(clazz, name, desc);
        for (String i : context.getInterfaceImplementors(clazz)) {
            if (info.isClassReferenced(i)) {
                processMethod(i, name, desc);
            }
        }
    }
}