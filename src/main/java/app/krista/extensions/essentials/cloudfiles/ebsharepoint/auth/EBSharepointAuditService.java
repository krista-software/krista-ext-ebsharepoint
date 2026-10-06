package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.msauth.extension.audit.AuditData;
import app.krista.sdk.msauth.extension.audit.AuditRecorder;
import app.krista.sdk.msauth.extension.service.MsAuthAuditService;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

/**
 * Thin delegate around the SDK's MsAuthAuditService — every audit feature is generic and provided by
 * the SDK; this class only wires it into HK2 with this extension's own KV namespace.
 */
@Service
public class EBSharepointAuditService implements AuditRecorder {

    private final MsAuthAuditService delegate;

    @Inject
    public EBSharepointAuditService(KeyValueStore keyValueStore) {
        this.delegate = new MsAuthAuditService(keyValueStore, "ebsharepoint");
    }

    @Override
    public void record(String invokerId, String eventType, String status, String catalogName,
                        String reason, String exceptionClass, Map<String, String> metadata) {
        delegate.record(invokerId, eventType, status, catalogName, reason, exceptionClass, metadata);
    }

    public void recordAuthError(String invokerId, String catalogName, MustAuthorizeException cause) {
        delegate.recordAuthError(invokerId, catalogName, cause);
    }

    public AuditData getData(String invokerId, Long fromTs, Long toTs, String type, String status) {
        return delegate.getData(invokerId, fromTs, toTs, type, status);
    }

    @Override
    public void resetAuthCounters() {
        delegate.resetAuthCounters();
    }

    public void clearRecords(String invokerId, Long fromTs, Long toTs) {
        delegate.clearRecords(invokerId, fromTs, toTs);
    }

    public void startRequest() {
        delegate.startRequest();
    }

    public Map<String, Object> getGlobalCounter() {
        return delegate.getGlobalCounter();
    }

    public java.util.List<Map<String, Object>> getArchiveIndex() {
        return delegate.getArchiveIndex();
    }

    public java.util.List<app.krista.sdk.msauth.extension.audit.AuditRecord> getArchivedRecords(String key) {
        return delegate.getArchivedRecords(key);
    }

    public void deleteArchive(String key) {
        delegate.deleteArchive(key);
    }

    public void archiveAll() {
        delegate.archiveAll();
    }
}
