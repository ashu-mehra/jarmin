package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

import org.objectweb.asm.ClassVisitor;

public class ArjunaAnnotationProcessor extends AnnotationProcessor {
    public ArjunaAnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(worklist, context, info, next);
        prefixes = new String[] { "Lcom/arjuna/common/internal/util/propertyservice/" };
        classAnnotations = new String[] {
            "PropertyPrefix"
        };
    }
}