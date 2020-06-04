package org.eclipse.openj9.jmin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HierarchyContext {
    private boolean closureComputed;
    private JarMap clazzToJar;
    private ServiceMap serviceClazzMap;
    private HashMap<String, String[]> classInterfaces;
    private ImplementorMap interfaceImplementorMap;
    private SubMap subMap;
    private SuperMap superMap;
    private HashSet<String> runtimeAnnotations;

    public HierarchyContext() {
        clazzToJar = new JarMap();
        serviceClazzMap = new ServiceMap();
        interfaceImplementorMap = new ImplementorMap();
        subMap = new SubMap();
        superMap = new SuperMap();
        runtimeAnnotations = new HashSet<String>();
        classInterfaces = new HashMap<String, String[]>();
    }

    public boolean addClassToJarMapping(String clazz, String jar) {
        if (!clazzToJar.containsKey(clazz)) {
            clazzToJar.put(clazz, jar);
            return true;
        }
        return false;
    }

    public String getJarForClass(String clazz) {
        return clazzToJar.get(clazz);
    }

    public Set<String> seenClasses() {
        return clazzToJar.keySet();
    }

    public void addServiceMap(String service, String clazz) {
        if (!serviceClazzMap.containsKey(service)) {
            serviceClazzMap.put(service, new ArrayList<String>());
        }
        serviceClazzMap.get(service).add(clazz);
    }

    public List<String> getServiceProviders(String service) {
        return serviceClazzMap.get(service);
    }

    public Set<String> getServiceInterfaces() {
        return serviceClazzMap.keySet();
    }

    public void addSuperClass(String clazz, String sup) {
        ArrayList<String> superclasses = new ArrayList<String>();
        if (sup != null) {
            superclasses.add(sup);
        }
        superMap.put(clazz, superclasses);
    }

    public List<String> getSuperClasses(String clazz) {
        assert closureComputed : "Cannot call for hierarchy information before closure computation is complete";
        if (superMap.containsKey(clazz)) {
            return superMap.get(clazz);
        }
        return java.util.Collections.emptyList();
    }

    public void addInterfaces(String clazz, String[] interfaces) {
        classInterfaces.put(clazz, interfaces);
    }

    private static String[] EMPTY_STR_ARRAY = new String[0];
    public String[] getClassInterfaces(String clazz) {
        if (classInterfaces.containsKey(clazz)) {
            return classInterfaces.get(clazz);
        }
        return EMPTY_STR_ARRAY;
    }

    public Set<String> getInterfaceImplementors(String clazz) {
        assert closureComputed : "Cannot call for hierarchy information before closure computation is complete";
        if (interfaceImplementorMap.containsKey(clazz)) {
            return interfaceImplementorMap.get(clazz);
        }
        return java.util.Collections.emptySet();
    }

    public void addRuntimeAnnotation(String clazz) {
        runtimeAnnotations.add(clazz);
    }

    public boolean hasRuntimeAnnotation(String clazz) {
        return runtimeAnnotations.contains(clazz);
    }

    public Set<String> getSubClasses(String clazz) {
        assert closureComputed : "Cannot call for hierarchy information before closure computation is complete";
        if (subMap.containsKey(clazz)) {
            return subMap.get(clazz);
        }
        return null;
    }
    
    public void computeClosure() {
        // construct closure of superclasses
        for (String c : superMap.keySet()) {
            ArrayList<String> itr = superMap.get(c);
            if (itr != null && itr.size() > 0) {
                for (itr = superMap.get(itr.get(0)); itr != null && itr.size() > 0; itr = superMap.get(itr.get(0))) {
                    superMap.get(c).addAll(itr);
                    if (itr.size() > 1)
                        break;
                }
            }
        }
        for (String c : superMap.keySet()) {
            for (String s : superMap.get(c)) {
                if (!subMap.containsKey(s)) {
                    subMap.put(s, new HashSet<String>());
                }
                subMap.get(s).add(c);
            }
        }
        
        // construct closure of interfaces
        for (String c : superMap.keySet()) {
            for (String s : superMap.get(c)) {
                if (classInterfaces.get(s) != null) {
                    for (String i : classInterfaces.get(s)) {
                        if (!interfaceImplementorMap.containsKey(i)) {
                            interfaceImplementorMap.put(i, new HashSet<String>());
                        }
                        interfaceImplementorMap.get(i).add(c);
                    }
                }
            }
            if (classInterfaces.get(c) != null) {
                for (String i : classInterfaces.get(c)) {
                    if (!interfaceImplementorMap.containsKey(i)) {
                        interfaceImplementorMap.put(i, new HashSet<String>());
                    }
                    interfaceImplementorMap.get(i).add(c);
                }
            }
        }
        
        for (String i : interfaceImplementorMap.keySet()) {
            if (classInterfaces.containsKey(i)) {
                for (String s : classInterfaces.get(i)) {
                    interfaceImplementorMap.get(s).addAll(interfaceImplementorMap.get(i));
                }
            }
        }
        closureComputed = true;
    }
}