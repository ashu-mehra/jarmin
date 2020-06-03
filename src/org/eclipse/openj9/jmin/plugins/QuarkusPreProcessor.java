package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class QuarkusPreProcessor extends PreProcessor {
    private static String[] interfaces = new String[] {
        "io/quarkus/arc/InjectableBean",
        "io/quarkus/arc/InjectableContext",
        "io/quarkus/arc/InjectableInstance",
        "io/quarkus/arc/InjectableInterceptor",
        "io/quarkus/arc/InjectableObserverMethod",
        "io/quarkus/arc/InjectableReferenceProvider"
    };
    
    public QuarkusPreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        super(worklist, context, info);
    }
    public void process() {
        for (String i : interfaces) {
            for (String c : context.getInterfaceImplementors(i)) {
                worklist.instantiateClass(c);
                for (MethodInfo mi : info.getClassInfo(c).getMethodsByNameOnly("<init>")) {
                    worklist.processMethod(c, mi.name(), mi.desc());
                }
            }
        }
    }
}