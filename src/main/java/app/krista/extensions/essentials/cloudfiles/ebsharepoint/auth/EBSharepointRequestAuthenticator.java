package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.ksdk.context.AuthorizationContext;
import app.krista.sdk.msauth.extension.base.AbstractRequestAuthenticator;
import app.krista.sdk.msauth.extension.base.MsAuthAttributes;
import app.krista.sdk.msauth.token.ITokenStore;

/**
 * Microsoft-auth-SDK request authenticator for EB Sharepoint.
 *
 * DELIBERATELY NOT @Service/@Inject — HK2 cannot bind the bare String invokerId/routingUrl this
 * constructor needs. Built with plain `new` inside EBSharepointExtension's constructor instead. See
 * adding-krista-msauth-tabs/SKILL.md Trap 0.6.
 */
public class EBSharepointRequestAuthenticator extends AbstractRequestAuthenticator {

    private final EBSharepointAttributeStore attributeStore;
    private final ITokenStore tokenStore;

    public EBSharepointRequestAuthenticator(EBSharepointAttributeStore attributeStore, String invokerId,
                                             AuthorizationContext authorizationContext, String routingUrl,
                                             ITokenStore tokenStore) {
        super(invokerId, routingUrl, "/ebsharepoint");
        this.attributeStore = attributeStore;
        this.tokenStore = tokenStore;
    }

    // Overload kept for callers that don't need a distinct token store (tests) — falls back to the
    // attribute store's own key, consistent with createDefaultAttributes below.
    public EBSharepointRequestAuthenticator(EBSharepointAttributeStore attributeStore, String invokerId,
                                             AuthorizationContext authorizationContext, String routingUrl) {
        this(attributeStore, invokerId, authorizationContext, routingUrl, null);
    }

    @Override
    protected MsAuthAttributes loadAttributes(String key) {
        return attributeStore.load(key);
    }

    @Override
    protected ITokenStore getTokenStore() {
        return tokenStore;
    }

    @Override
    protected MsAuthAttributes createDefaultAttributes(String routingUrl) {
        return new EBSharepointMsAuthAttributes(null, null, null, null, "Public", routingUrl, "krista");
    }
}
