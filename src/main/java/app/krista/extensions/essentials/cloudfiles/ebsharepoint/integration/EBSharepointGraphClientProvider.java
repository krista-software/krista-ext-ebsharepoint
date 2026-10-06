package app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth.EBSharepointAttributeStore;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth.EBSharepointAuditService;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth.EBSharepointMsAuthAttributes;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.msauth.extension.service.MsAuthGraphClientProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Thin alias over the SDK's MsAuthGraphClientProvider. This extension is single-mailbox-per-invoker
 * (one SharePoint connection configured once via the Authentication tab, no per-end-user delegation
 * model requested) — the operator path is fully implemented; forRequest() still dispatches on
 * invokeAsUser() so the failure mode for an unsupported per-user call is a clear MustAuthorizeException,
 * never an NPE or a silent wrong-tenant read. See adding-krista-msauth-tabs/SKILL.md Trap 0.9/0.12/0.16.
 *
 * MsAuthGraphClientProvider is NEVER @Inject-ed directly (Trap 0.16) — always constructed manually,
 * per call, from attrs freshly loaded via the AttributeStore.
 */
@Service
public class EBSharepointGraphClientProvider {

    private final KeyValueStore keyValueStore;
    private final EBSharepointAttributeStore attributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final EBSharepointAuditService auditService;

    @Inject
    public EBSharepointGraphClientProvider(KeyValueStore keyValueStore, EBSharepointAttributeStore attributeStore,
                                            RequestContext requestContext, AuthorizationContext authorizationContext,
                                            EBSharepointAuditService auditService) {
        this.keyValueStore = keyValueStore;
        this.attributeStore = attributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.auditService = auditService;
    }

    /** Operator/admin context — the single connection configured via the Authentication tab. */
    public GraphServiceClient<okhttp3.Request> forOperator(String invokerId) throws IOException {
        EBSharepointMsAuthAttributes attrs = attributeStore.load(invokerId);
        if (attrs == null) {
            throw new MustAuthorizeException(
                    "Not configured — complete the Authentication tab first.", List.of());
        }
        MsAuthGraphClientProvider provider = new MsAuthGraphClientProvider(
                keyValueStore, attrs, requestContext, authorizationContext, invokerId);
        provider.setAuditService(auditService);
        // useSetupEmail=true — NEVER false/null here (Trap 0.9): AuthorizationContext's authorized
        // account is only populated during a real end-user's live catalog request.
        return provider.getGraphServiceClientForUser(true, null);
    }

    /** Every real catalog request routes through here, dispatching on RequestContext#invokeAsUser(). */
    public GraphServiceClient<okhttp3.Request> forRequest(RequestContext requestContext, String invokerId)
            throws IOException {
        if (!requestContext.invokeAsUser()) {
            return forOperator(invokerId);
        }
        throw new MustAuthorizeException(
                "Sign-in on behalf of this user isn't supported by this extension — it's connected once, "
                        + "for the whole team, via the Authentication tab.", List.of());
    }
}
