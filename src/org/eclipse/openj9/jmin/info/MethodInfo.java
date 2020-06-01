package org.eclipse.openj9.jmin.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodInfo {
    private final String name;
    private final String desc;
    private final ArrayList<CallSite> callsites;
    private final HashSet<String> referencedClasses;
    private final ArrayList<FieldSite> referencedFields;
    private final HashSet<String> annotations;
    private boolean referenced;
    private boolean processed;
    public MethodInfo(String name, String desc) {
        this.name = name;
        this.desc = desc;
        this.callsites = new ArrayList<CallSite>();
        this.referencedClasses = new HashSet<String>();
        this.referencedFields = new ArrayList<FieldSite>();
        this.annotations = new HashSet<String>();
        this.referenced = false;
        this.processed = false;
    }
    public String name() { return name; }
    public String desc() { return desc; }
    public void setReferenced() {
        referenced = true;
    }
    public void setProcessed() {
        this.processed = true;
    }
    public boolean referenced() {
        return this.referenced;
    }
    public boolean processed() {
        return this.processed;
    }
    public void addCallSite(String clazz, String name, String desc, CallKind kind) {
        callsites.add(new CallSite(clazz, name, desc, kind));
    }
    public void addReferencedClass(String clazz) {
        referencedClasses.add(clazz);
    }
    public void addReferencedField(String clazz, String name, String desc, FieldKind kind) {
        referencedFields.add(new FieldSite(clazz, name, desc, kind));
    }
    public List<CallSite> getCallSites() {
        return callsites;
    }
    public List<FieldSite> getReferencedFields() {
        return referencedFields;
    }
    public Set<String> getReferencedClasses() {
        return referencedClasses;
    }
    public void addAnnotation(String annot) {
        annotations.add(annot);
    }
    public HashSet<String> getAnnotations() {
        return annotations;
    }
    @Override
    public String toString() {
        return name + desc + " -" + (referenced ? "" : " not") + " referenced" + (processed ? "" : " not") + " processsed";
    }
}