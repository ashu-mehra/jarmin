package org.eclipse.openj9.jmin.plugins;

import java.util.Set;

import org.eclipse.openj9.jmin.info.ClassInfo;
import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class HibernatePreProcessor {
    private WorkList worklist;
    private HierarchyContext context;
    private ReferenceInfo info;
    public HibernatePreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        this.worklist = worklist;
        this.context = context;
        this.info = info;
    }
    public void process() {
        for (String svc : context.getInterfaceImplementors("org/hibernate/service/Service")) {
            for (MethodInfo mi : info.getClassInfo(svc).getMethodsByNameOnly("<init>")) {
                worklist.processMethod(svc, "<init>", mi.desc());
            }
        }
        if (context.getInterfaceImplementors("org/hibernate/service/spi/Startable").size() > 0) {
            worklist.processInterfaceMethod("org/hibernate/service/spi/Startable", "start", "()V");
        }
        if (context.getInterfaceImplementors("org/hibernate/service/spi/Stoppable").size() > 0) {
            worklist.processInterfaceMethod("org/hibernate/service/spi/Stoppable", "stop", "()V");
        }
        if (context.getInterfaceImplementors("org/hibernate/service/spi/Wrapped").size() > 0) {
            worklist.processInterfaceMethod("org/hibernate/service/spi/Wrapped", "unwrap", "(Ljava/lang/Class;)Ljava/lang/Object;");
        }
        if (context.getInterfaceImplementors("org/hibernate/service/spi/Configurable").size() > 0) {
            worklist.processInterfaceMethod("org/hibernate/service/spi/Configurable", "configure", "(Ljava/util/Map;)V");
        }
        {
            Set<String> implementors = context.getInterfaceImplementors("org/hibernate/boot/registry/StandardServiceInitiator");
            if (implementors.size() > 0) {
                worklist.processInterfaceMethod("org/hibernate/boot/registry/StandardServiceInitiator", "initiateService", "(Ljava/util/Map;Lorg/hibernate/service/spi/ServiceRegistryImplementor;)Lorg/hibernate/service/Service;");
                for (String svcInit : implementors) {
                    for (MethodInfo mi : info.getClassInfo(svcInit).getMethodsByNameOnly("<init>")) {
                        worklist.processMethod(svcInit, "<init>", mi.desc());
                    }
                }
            }
        }
        {
            Set<String> implementors = context.getInterfaceImplementors("org/hibernate/persister/spi/PersisterFactory");
            if (implementors.size() > 0) {
                worklist.processInterfaceMethod("org/hibernate/persister/spi/PersisterFactory", "createEntityPersister", "(Lorg/hibernate/mapping/PersistentClass;Lorg/hibernate/cache/spi/access/EntityDataAccess;Lorg/hibernate/cache/spi/access/NaturalIdDataAccess;Lorg/hibernate/persister/spi/PersisterCreationContext;)Lorg/hibernate/persister/entity/EntityPersister;");
                worklist.processInterfaceMethod("org/hibernate/persister/spi/PersisterFactory", "createCollectionPersister", "(Lorg/hibernate/mapping/Collection;Lorg/hibernate/cache/spi/access/CollectionDataAccess;Lorg/hibernate/persister/spi/PersisterCreationContext;)Lorg/hibernate/persister/collection/CollectionPersister;");
                for (String pf : implementors) {
                    for (MethodInfo mi : info.getClassInfo(pf).getMethodsByNameOnly("<init>")) {
                        worklist.processMethod(pf, "<init>", mi.desc());
                    }
                }
            }
        }
        // reflective creation from constructor in PersisterFactoryImpl
        for (String ef : context.getInterfaceImplementors("org/hibernate/persister/entity/EntityPersister")) {
            for (MethodInfo mi : info.getClassInfo(ef).getMethodsByNameOnly("<init>")) {
                worklist.processMethod(ef, "<init>", mi.desc());
            }
        }
        if (context.getSubClasses("io/quarkus/runtime/Application") != null) {
            for (String app : context.getSubClasses("io/quarkus/runtime/Application")) {
                for (MethodInfo mi : info.getClassInfo(app).getMethodsByNameOnly("<init>")) {
                    worklist.processMethod(app, "<init>", mi.desc());
                }
            }
        }
        //BeanValidationIntegrator - called by reflection
        ClassInfo tsa = info.getClassInfo("org/hibernate/cfg/beanvalidation/TypeSafeActivator");
        if (tsa != null) {
            for (MethodInfo mi : tsa.getMethodsByNameOnly("activate")) {
                worklist.processVirtualMethod("org/hibernate/cfg/beanvalidation/TypeSafeActivator", "activate", mi.desc());
            }
        
            for (MethodInfo mi : tsa.getMethodsByNameOnly("validateSuppliedFactory")) {
                worklist.processVirtualMethod("org/hibernate/cfg/beanvalidation/TypeSafeActivator", "validateSuppliedFactory", mi.desc());
            }
        }
        //Reflective instantiation in org/hibernate/tuple/component/ComponentTuplizerFactory.java based on the entityMode - preload all the implementations
        for (String i : context.getInterfaceImplementors("org/hibernate/tuple/Instantiator")) {
            worklist.processClass(i);
        }
        // unknown path
        worklist.processClass("org/hibernate/internal/SessionImpl$LobHelperImpl");
        // passed by map as hibernate.dialect in init
        worklist.processClass("io/quarkus/hibernate/orm/runtime/dialect/QuarkusPostgreSQL95Dialect");
    }
}