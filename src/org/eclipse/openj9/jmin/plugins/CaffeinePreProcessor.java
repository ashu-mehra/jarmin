package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class CaffeinePreProcessor {
    private WorkList worklist;
    private HierarchyContext context;
    private ReferenceInfo info;
    public CaffeinePreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        this.worklist = worklist;
        this.context = context;
        this.info = info;
    }
    public void process() {
        if (context.getSubClasses("com/github/benmanes/caffeine/cache/Node") != null) {
            for (String clazz : context.getSubClasses("com/github/benmanes/caffeine/cache/Node")) {
                for (MethodInfo mi : info.getClassInfo(clazz).getMethodsByNameOnly("<init>")) {
                    worklist.processMethod(clazz, "<init>", mi.desc());
                }
            }
        }
        if (context.getSubClasses("com/github/benmanes/caffeine/cache/BoundedLocalCache") != null) {
            for (String clazz : context.getSubClasses("com/github/benmanes/caffeine/cache/BoundedLocalCache")) {
                for (MethodInfo mi : info.getClassInfo(clazz).getMethodsByNameOnly("<init>")) {
                    worklist.processMethod(clazz, "<init>", mi.desc());
                }
            }
        }
    }
}