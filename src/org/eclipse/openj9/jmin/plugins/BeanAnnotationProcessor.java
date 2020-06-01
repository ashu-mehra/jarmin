package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.objectweb.asm.ClassVisitor;

public class BeanAnnotationProcessor extends AnnotationProcessor {
    public BeanAnnotationProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info, ClassVisitor next) {
        super(worklist, context, info, next);
        prefixes = new String[] { "Ljavax/", "Ljakarta/" };
        classAnnotations = new String[] {
            "annotation/ManagedBean",
            "enterprise/context/ApplicationScoped",
            "enterprise/context/SessionScoped",
            "enterprise/context/ConversationScoped",
            "enterprise/context/RequestScoped",
            "enterprise/context/NormalScope",
            "interceptor/Interceptor",
            "decorator/Decorator",
            "enterprise/context/Dependent",
            "enterprise/inject/Alternative",
            "inject/Singleton",
            "inject/Scope",
            "inject/Inject",
            "ws/rs/Produces",   
            "ws/rs/Consumes",
            "ws/rs/ext/Provider"
        };
        constructorAnnotations = new String[] {
            "inject/Inject"
        };
        methodAnnotations = new String[] {
            "enterprise/inject/Produces",
            "enterprise/inject/Specializes",
            "inject/Inject",
            "ws/rs/HttpMethod",
            "ws/rs/GET",
            "ws/rs/POST",
            "ws/rs/PUT",
            "ws/rs/DELETE",
            "ws/rs/PATCH",
            "ws/rs/Path",
            "annotation/PreDestroy",
            "annotation/PostConstruct",
            "inject/AroundInvoke"
        };
        methodParameterAnnotations = new String [] {
            "enterprise/event/Observes"
        };
        fieldAnnotations = new String[] {
            "enterprise/inject/Produces",
            "inject/Inject"
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
        if (context.getInterfaceImplementors("javax/ws/rs/container/DynamicFeature").contains(name)) {
            worklist.processMethod(name, "configure", "(Ljavax/ws/rs/container/ResourceInfo;Ljavax/ws/rs/core/FeatureContext;)V");
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }
}