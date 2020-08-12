package org.eclipse.openj9.jmin.plugins;

import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;

public class ApacheCXFPreProcessor extends PreProcessor {
    public ApacheCXFPreProcessor(WorkList worklist, HierarchyContext context, ReferenceInfo info) {
        super(worklist, context, info);
    }

    @Override
    public void process() {
        // Classes present in cxf-core.jar:META-INF/cxf/bus-extensions.txt
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/PhaseManagerImpl");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/WorkQueueManagerImpl");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/CXFBusLifeCycleManager");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/ServerRegistryImpl");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/EndpointResolverRegistryImpl");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/HeaderManagerImpl");
        worklist.forceInstantiateClass("org/apache/cxf/service/factory/FactoryBeanListenerManager");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/ServerLifeCycleManagerImpl");
        worklist.forceInstantiateClass("org/apache/cxf/bus/managers/ClientLifeCycleManagerImpl");
        worklist.forceInstantiateClass("org/apache/cxf/bus/resource/ResourceManagerImpl");
        worklist.forceInstantiateClass("org/apache/cxf/catalog/OASISCatalogManager");

        // Classes present in cxf-rt-rs-sse.jar:META-INF/cxf/bus-extensions.txt
        worklist.forceInstantiateClass("org/apache/cxf/transport/sse/SseProvidersExtension");

        // Classes present in cxf-rt-transports-http.jar:META-INF/cxf/bus-extensions.txt
        worklist.forceInstantiateClass("org/apache/cxf/transport/http/HTTPTransportFactory");
        worklist.forceInstantiateClass("org/apache/cxf/transport/http/HTTPWSDLExtensionLoader");
        worklist.forceInstantiateClass("org/apache/cxf/transport/http/policy/HTTPClientAssertionBuilder");
        worklist.forceInstantiateClass("org/apache/cxf/transport/http/policy/HTTPServerAssertionBuilder");
        worklist.forceInstantiateClass("org/apache/cxf/transport/http/policy/NoOpPolicyInterceptorProvider");
    }
}