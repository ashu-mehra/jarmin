package org.eclipse.openj9.jmin.util;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.openj9.JMin;
import org.eclipse.openj9.jmin.WorkItem;
import org.eclipse.openj9.jmin.info.ClassInfo;
import org.eclipse.openj9.jmin.info.FieldInfo;
import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;

public class WorkList {
    private String reductionMode;
    private String inclusionMode;
    private HierarchyContext context;
    private ReferenceInfo info;
    private LinkedList<WorkItem> worklist;

    public WorkList(String reductionMode, ReferenceInfo info, HierarchyContext context) {
        this.reductionMode = reductionMode.intern();
        this.inclusionMode = System.getProperty(JMin.INCLUSION_MODE_PROPERTY_NAME);
        if (this.inclusionMode == null) {
            this.inclusionMode = "instantiation";
        } else {
            this.inclusionMode = this.inclusionMode.intern();
        }
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

    private void processMethods(String c) {
        ClassInfo cinfo = info.getClassInfo(c);

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
                        cinfo.markMethodReferenced(mi.name(), mi.desc());
                        worklist.add(new WorkItem(c, mi.name(), mi.desc()));
                    }
                }
            }
        }

        if (reductionMode != "field") {
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
        }
    }

    public void instantiateClass(String clazz) {
        processClass(clazz);
        if (!info.isClassInstantiated(clazz)) {
            ClassInfo cinfo = info.getClassInfo(clazz);
            if (cinfo == null) {
                handleClass(clazz);
            }
            cinfo = info.getClassInfo(clazz);
            cinfo.setInstantiated();

            List<String> superClazzes = context.getSuperClasses(clazz);
            if (superClazzes != null && superClazzes.size() > 0) {
                instantiateClass(superClazzes.get(0));
            }

            if (context.getServiceProviders(clazz) != null) {
                for (String impl : context.getServiceProviders(clazz)) {
                    instantiateClass(impl);
                }
            } else if (clazz.equals(JMin.ALL_SVC_IMPLEMENTAIONS)) {
                for (String svc : context.getServiceInterfaces()) {
                    for (String impl : context.getServiceProviders(svc)) {
                        instantiateClass(impl);
                    }
                }
            }

            if (inclusionMode == "instantiation") {
                processMethods(clazz);
            }
        }
    }

    private void handleClass(String c) {
        if (!info.isClassReferenced(c)) {
            info.addClass(c).setReferenced();
            if (info.getClassInfo(c).hasMethod("<clinit>", "()V") 
                && !info.getClassInfo(c).isMethodReferenced("<clinit>", "()V")) {
                worklist.add(new WorkItem(c, "<clinit>", "()V"));
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
                processClass(check);
                ClassInfo cinfo = info.getClassInfo(check);
                for (String annot : cinfo.getAnnotations()) {
                    processClass(annot);
                }
            }
        }
    }

    public void processClass(String clazz) {
        /*if (clazz.indexOf('.') != -1) {
            clazz = clazz.replace('.', '/');
            throw new RuntimeException("bad class name" + clazz);
        }*/
        if (!info.isClassReferenced(clazz)) {
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

            if (inclusionMode == "reference") {
                processMethods(clazz);
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
            /*if (clazz.equals("io/netty/channel/socket/nio/NioSocketChannel") && name.equals("<init>")) {
                throw new RuntimeException(desc);
            }*/
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
                if ((inclusionMode == "reference" && info.isClassReferenced(c))
                    || (inclusionMode == "instantiation" && info.isClassInstantiated(c))) {
                    processMethod(c, name, desc);
                }
            }
        }
    }

    public void processInterfaceMethod(String clazz, String name, String desc) {
        processMethod(clazz, name, desc);
        for (String i : context.getInterfaceImplementors(clazz)) {
            if ((inclusionMode == "reference" && info.isClassReferenced(i))
                || (inclusionMode == "instantiation" && info.isClassInstantiated(i))) {
                processMethod(i, name, desc);
            }
        }
    }
}