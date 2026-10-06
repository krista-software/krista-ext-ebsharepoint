package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.extension.executor.Invoker;
import app.krista.sdk.msauth.extension.audit.AbstractAuditResource;
import app.krista.sdk.msauth.extension.audit.AuditData;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.HashMap;
import java.util.Map;

/** Serves the microsoft-auth-sdk's "Audit" tab. Class-level @Path matches customTabs()'s entry. */
@Service
@Path("/audit")
public class EBSharepointAuditResource extends AbstractAuditResource {

    private final EBSharepointAuditService auditService;
    private final EBSharepointAttributeStore attributeStore;

    @Inject
    public EBSharepointAuditResource(EBSharepointAuditService auditService,
                                      EBSharepointAttributeStore attributeStore, Invoker invoker) {
        super(invoker.getInvokerId());
        this.auditService = auditService;
        this.attributeStore = attributeStore;
    }

    @Override
    protected AuditData getAuditData(String invokerId, Long from, Long to, String type, String status) {
        return auditService.getData(invokerId, from, to, type, status);
    }

    @Override
    protected void clearAuditRecords(String invokerId, Long from, Long to) {
        auditService.clearRecords(invokerId, from, to);
    }

    @Override
    protected Map<String, Object> buildHealthResponse() {
        Map<String, Object> health = new HashMap<>();
        EBSharepointMsAuthAttributes attrs = attributeStore.load(getInvokerId());
        if (attrs != null) {
            health.put("authType", attrs.getAuthType());
            health.put("email", attrs.getEmail());
            health.put("tenantId", mask(attrs.getTenantId()));
            health.put("clientId", mask(attrs.getClientId()));
        } else {
            health.put("authType", "not configured");
        }
        return health;
    }

    @Override
    protected java.util.List<java.util.Map<String, Object>> getArchiveIndex() {
        return auditService.getArchiveIndex();
    }

    @Override
    protected java.util.List<app.krista.sdk.msauth.extension.audit.AuditRecord> getArchivedRecords(String archiveKey) {
        return auditService.getArchivedRecords(archiveKey);
    }

    @Override
    protected void deleteArchiveEntry(String archiveKey) {
        auditService.deleteArchive(archiveKey);
    }

    @Override
    protected Map<String, Object> getGlobalCounter() {
        return auditService.getGlobalCounter();
    }

    @Override
    protected void archiveAllRecords() {
        auditService.archiveAll();
    }
}
