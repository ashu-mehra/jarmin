package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

import java.util.Set;

public class OsgiPreProcessor extends PreProcessor {
    public OsgiPreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        super(worklist, context, info);
    }

    @Override
    public void process() {
        Set<String> implementors = context.getInterfaceImplementors("org/osgi/framework/BundleActivator");
        if (implementors.size() > 0) {
            for (String opt : implementors) {
                worklist.forceInstantiateClass(opt);
            }
            worklist.processInterfaceMethod("org/osgi/framework/BundleActivator", "start", "(Lorg/osgi/framework/BundleContext;)V");
            worklist.processInterfaceMethod("org/osgi/framework/BundleActivator", "stop", "(Lorg/osgi/framework/BundleContext;)V");
        }
    }
}
