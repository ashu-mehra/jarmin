package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.ClassVisitor;

public class PersistenceAnnotationProcessor extends AnnotationProcessor {
    public PersistenceAnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(worklist, context, info, next);
        prefixes = new String[] { "Ljavax/persistence/" };
        classAnnotations = new String[] {
          "Entity"  
        };
    }
    
    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) {
        if (context.getInterfaceImplementors("org/hibernate/dialect/lock/LockingStrategy").contains(name)) {
            worklist.instantiateClass(name);
            worklist.processMethod(name, "lock", "(Ljava/io/Serializable;Ljava/lang/Object;Ljava/lang/Object;ILorg/hibernate/engine/spi/SharedSessionContractImplementor;)V");
        }
        super.visit(version,access,name,signature,superName, interfaces);
    }
}