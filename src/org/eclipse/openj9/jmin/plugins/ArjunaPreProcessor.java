package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class ArjunaPreProcessor {
    private WorkList worklist;
    private HierarchyContext context;
    public ArjunaPreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        this.worklist = worklist;
        this.context = context;
    }
    public void process() {
        // com/arjuna/ats/jta/common/JTAEnvironmentBean - see reflective invocation
        for (String i : context.getInterfaceImplementors("javax/transaction/UserTransaction")) {
            worklist.processClass(i);
        }
    }
}