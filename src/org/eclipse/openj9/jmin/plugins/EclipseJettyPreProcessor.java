package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class EclipseJettyPreProcessor extends PreProcessor {
    public EclipseJettyPreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        super(worklist, context, info);
    }

    @Override
    public void process() {
        /* Slf4jLog gets loaded via Properties.getProperties() method,
         * something like
         *  Loader.loadClass(Properties.getProperties("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Slf4jLog");
         */
        worklist.instantiateClass("org/eclipse/jetty/util/log/Slf4jLog");
    }
}
