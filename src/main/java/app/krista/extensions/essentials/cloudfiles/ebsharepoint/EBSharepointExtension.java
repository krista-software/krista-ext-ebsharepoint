package app.krista.extensions.essentials.cloudfiles.ebsharepoint;

import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.ChangeLog;
import app.krista.extension.impl.anno.Containerize;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.Java;
import app.krista.extension.impl.anno.StaticResource;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth.EBSharepointAttributeStore;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth.EBSharepointRequestAuthenticator;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth.EBSharepointTokenStore;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.EBSharepointGraphClientProvider;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.GraphErrorTranslator;
import com.microsoft.graph.requests.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * EB Sharepoint extension entry point (Microsoft-auth-SDK, Pattern D).
 *
 * Connection credentials are NOT standard Setup-tab @Field.* fields — they are collected through the
 * custom Authentication tab (see auth.EBSharepointAuthResource) and persisted via
 * auth.EBSharepointAttributeStore, per the adding-krista-msauth-tabs skill. This class only wires the
 * Setup-tab lifecycle hooks (Test Connection, custom tabs, the Authenticator) around that.
 */
@Extension(name = "EB Sharepoint", version = "1.0.0",
        implementationModel = Extension.ImplementationModel.Krista_4_O)
@Containerize(baseImageVersion = "3.6.2-sp3")
@StaticResource(path = "docs", file = "docs")
@ChangeLog(file = "docs/pages/ReleaseNotes.md")
@Java(version = Java.Version.JAVA_21)
public final class EBSharepointExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(EBSharepointExtension.class);

    private final Invoker invoker;
    private final EBSharepointAttributeStore attributeStore;
    private final EBSharepointGraphClientProvider graphClientProvider;
    private final EBSharepointRequestAuthenticator requestAuthenticator;

    @Inject
    public EBSharepointExtension(Invoker invoker, EBSharepointAttributeStore attributeStore,
                                  EBSharepointGraphClientProvider graphClientProvider,
                                  EBSharepointTokenStore tokenStore,
                                  AuthorizationContext authorizationContext) {
        this.invoker = invoker;
        this.attributeStore = attributeStore;
        this.graphClientProvider = graphClientProvider;
        // Built with plain `new`, never @Inject-ed — HK2 cannot bind the bare String invokerId/
        // routingUrl this authenticator's constructor needs. See Trap 0.6.
        String routingUrl = invoker.getRoutingInfo()
                .getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.requestAuthenticator = new EBSharepointRequestAuthenticator(
                attributeStore, invoker.getInvokerId(), authorizationContext, routingUrl, tokenStore);
    }

    /** Setup-tab tabs: Authentication, Audit, Documentation. See custom-tabs.md / Trap 0. */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of(
                "Authentication", "rest/ebsharepoint/authentication/",
                "Documentation", "static/docs",
                "EB Sharepoint Audit Dashboard", "rest/ebsharepoint/audit/");
    }

    /**
     * The Setup tab's OWN Test Connection hook — checks the ALREADY-SAVED configuration. Distinct from
     * the Authentication tab's own Test Connection, which checks the candidate/not-yet-saved form data
     * (see EBSharepointAuthResource#testConnection). Both must work independently — Trap 0.7.
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        String invokerId = invoker.getInvokerId();
        if (attributeStore.load(invokerId) == null) {
            throw new IllegalStateException(
                    "Not configured — please authenticate via the Authentication tab first.");
        }
        try {
            GraphServiceClient<okhttp3.Request> client = graphClientProvider.forOperator(invokerId);
            // Same resource family the real business logic uses (drives/sites) — never `.me()`, and
            // never a bare `.users(email)` directory lookup. See Trap 0.14.
            client.sites().buildRequest().top(1).get();
            LOGGER.info("Setup-tab Test Connection succeeded for invoker {}", invokerId);
        } catch (Exception e) {
            LOGGER.error("Setup-tab Test Connection failed for invoker {}", invokerId, e);
            throw new IllegalStateException(GraphErrorTranslator.toFriendlyMessage(e), e);
        }
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }
}
