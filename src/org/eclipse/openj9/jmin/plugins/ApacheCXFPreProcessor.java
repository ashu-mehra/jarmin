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
        worklist.instantiateClass("org/apache/cxf/bus/managers/PhaseManagerImpl");
        worklist.instantiateClass("org/apache/cxf/bus/managers/WorkQueueManagerImpl");
        worklist.instantiateClass("org/apache/cxf/bus/managers/CXFBusLifeCycleManager");
        worklist.instantiateClass("org/apache/cxf/bus/managers/ServerRegistryImpl");
        worklist.instantiateClass("org/apache/cxf/bus/managers/EndpointResolverRegistryImpl");
        worklist.instantiateClass("org/apache/cxf/bus/managers/HeaderManagerImpl");
        worklist.instantiateClass("org/apache/cxf/service/factory/FactoryBeanListenerManager");
        worklist.instantiateClass("org/apache/cxf/bus/managers/ServerLifeCycleManagerImpl");
        worklist.instantiateClass("org/apache/cxf/bus/managers/ClientLifeCycleManagerImpl");
        worklist.instantiateClass("org/apache/cxf/bus/resource/ResourceManagerImpl");
        worklist.instantiateClass("org/apache/cxf/catalog/OASISCatalogManager");

        // Classes present in cxf-rt-rs-sse.jar:META-INF/cxf/bus-extensions.txt
        worklist.instantiateClass("org/apache/cxf/transport/sse/SseProvidersExtension");

        // Classes present in cxf-rt-transports-http.jar:META-INF/cxf/bus-extensions.txt
        worklist.instantiateClass("org/apache/cxf/transport/http/HTTPTransportFactory");
        worklist.instantiateClass("org/apache/cxf/transport/http/HTTPWSDLExtensionLoader");
        worklist.instantiateClass("org/apache/cxf/transport/http/policy.HTTPClientAssertionBuilder");
        worklist.instantiateClass("org/apache/cxf/transport/http/policy.HTTPServerAssertionBuilder");
        worklist.instantiateClass("org/apache/cxf/transport/http/policy.NoOpPolicyInterceptorProvider");
    }
}