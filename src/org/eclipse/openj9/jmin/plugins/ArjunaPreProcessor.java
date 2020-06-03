package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class ArjunaPreProcessor extends PreProcessor {
    public ArjunaPreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        super(worklist, context, info);
    }
    public void process() {
        // com/arjuna/ats/jta/common/JTAEnvironmentBean - see reflective invocation
        for (String i : context.getInterfaceImplementors("javax/transaction/UserTransaction")) {
            worklist.instantiateClass(i);
        }
    }
}