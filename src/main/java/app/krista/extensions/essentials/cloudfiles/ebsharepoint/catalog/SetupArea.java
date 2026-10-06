package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.GraphErrorTranslator;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.SharePointGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

/** Setup area — Test Connection + Health Check, the standard connectivity requests. */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
public class SetupArea {

    private static final Logger LOG = LoggerFactory.getLogger(SetupArea.class);

    private final SharePointGraphService graphService;
    private final String invokerId;

    @Inject
    public SetupArea(SharePointGraphService graphService, Invoker invoker) {
        this.graphService = graphService;
        this.invokerId = invoker.getInvokerId();
    }

    @CatalogRequest(
            id = "localDomainRequest_eb5p0000-aaaa-4aaa-8aaa-000000000004",
            name = "Test Connection",
            description = "Validate connectivity and authentication against Microsoft SharePoint",
            area = "Setup",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse testConnection() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            graphService.probeConnectivity();
            out.put("Success", true);
            out.put("Message", "Connected to Microsoft SharePoint.");
        } catch (Exception e) {
            LOG.error("Test connection failed for invoker {}", invokerId, e);
            out.put("Success", false);
            out.put("Message", GraphErrorTranslator.toFriendlyMessage(e));
        }
        return new ExtensionResponseBuilder().success(out).build();
    }

    @CatalogRequest(
            id = "localDomainRequest_eb5p0000-aaaa-4aaa-8aaa-000000000005",
            name = "Health Check",
            description = "Check extension and SharePoint connectivity health",
            area = "Setup",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Boolean(name = "Is Healthy", required = false)
    @Field(name = "Heap Used MB", type = "Number", required = false)
    @Field.Text(name = "Details", required = false)
    public ExtensionResponse healthCheck() {
        Map<String, Object> out = new LinkedHashMap<>();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long heapUsedMb = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        boolean healthy;
        String details;
        try {
            graphService.probeConnectivity();
            healthy = true;
            details = "OK — SharePoint reachable.";
        } catch (Exception e) {
            LOG.error("Health check failed for invoker {}", invokerId, e);
            healthy = false;
            details = GraphErrorTranslator.toFriendlyMessage(e);
        }
        out.put("Is Healthy", healthy);
        out.put("Heap Used MB", (double) heapUsedMb);
        out.put("Details", details);
        return new ExtensionResponseBuilder().success(out).build();
    }
}
