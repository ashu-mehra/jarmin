package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public abstract class PreProcessor {
    protected WorkList worklist;
    protected HierarchyContext context;
    protected ReferenceInfo info;
    public PreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        this.worklist = worklist;
        this.context = context;
        this.info = info;
    }
    abstract public void process();
}