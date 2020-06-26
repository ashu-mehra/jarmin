package org.eclipse.openj9.jmin.info;

import org.eclipse.openj9.jmin.methodsummary.MethodSummary;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodInfo {
    private final String clazz;
    private final String name;
    private final String desc;
    private final ArrayList<CallSite> callsites;
    private final ArrayList<CallSite> callers;
    private final HashSet<String> referencedClasses;
    private final ArrayList<FieldSite> referencedFields;
    private final HashSet<String> annotations;
    private final HashSet<String> instantiatedTypes;
    private MethodSummary summary;
    private boolean referenced;
    private boolean processed;
    private boolean processedForSummary;

    public MethodInfo(String clazz, String name, String desc) {
        this.clazz = clazz;
        this.name = name;
        this.desc = desc;
        this.callsites = new ArrayList<CallSite>();
        this.callers = new ArrayList<CallSite>();
        this.referencedClasses = new HashSet<String>();
        this.referencedFields = new ArrayList<FieldSite>();
        this.annotations = new HashSet<String>();
        this.instantiatedTypes = new HashSet<String>();
        this.summary = null;
        this.referenced = false;
        this.processed = false;
        this.processedForSummary = false;
    }
    public String clazz() { return clazz; }
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
    public void addCallSite(String clazz, String name, String desc, CallKind kind, int instuctionIndex) {
        callsites.add(new CallSite(this, clazz, name, desc, kind, instuctionIndex));
    }
    public void addCaller(CallSite callSite) {
        callers.add(callSite);
    }
    public List<CallSite> getCallers() {
        assert ReferenceInfo.callersComputed : "Cannot access caller before it is computed";
        return callers;
    }
    public void addReferencedClass(String clazz) {
        referencedClasses.add(clazz.replace('.', '/'));
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
    public void addInstantiatedClass(String clazz) {
        instantiatedTypes.add(clazz.replace('.', '/'));
    }
    public HashSet<String> getInstantiatedClasses() {
        return instantiatedTypes;
    }

    @Override
    public String toString() {
        return name + desc + " -" + (referenced ? "" : " not") + " referenced" + (processed ? "" : " not") + " processsed";
    }

    public MethodSummary getMethodSummary() {
        if (summary == null) {
            summary = new MethodSummary();
        }
        return summary;
    }
    public boolean hasMethodSummary() {
        return summary != null;
    }
    public void setProcessedForSummary() {
        processedForSummary = true;
    }
    public boolean isProcessedForSummary() { return processedForSummary; }
}