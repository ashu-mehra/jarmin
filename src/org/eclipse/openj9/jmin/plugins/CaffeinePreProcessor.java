package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class CaffeinePreProcessor extends PreProcessor {
    public CaffeinePreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        super(worklist, context, info);
    }
    public void process() {
        if (context.getSubClasses("com/github/benmanes/caffeine/cache/Node") != null) {
            for (String clazz : context.getSubClasses("com/github/benmanes/caffeine/cache/Node")) {
                worklist.forceInstantiateClass(clazz);
            }
        }
        if (context.getSubClasses("com/github/benmanes/caffeine/cache/BoundedLocalCache") != null) {
            for (String clazz : context.getSubClasses("com/github/benmanes/caffeine/cache/BoundedLocalCache")) {
                worklist.forceInstantiateClass(clazz);
            }
        }
    }
}