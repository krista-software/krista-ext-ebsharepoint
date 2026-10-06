package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.sdk.msauth.extension.base.MsAuthAttributes;
import app.krista.sdk.msauth.extension.credentials.PublicCredentialStore;

import java.util.HashMap;
import java.util.Map;

/**
 * EB Sharepoint's concrete MsAuthAttributes — Pattern A (hand-rolled), `implements MsAuthAttributes`
 * directly (not `extends BaseAttributes`). The Quick-Setup fallback goes through the SDK's own
 * PublicCredentialStore (msauth-public.properties), not a hand-rolled config.properties reader.
 * See adding-krista-msauth-tabs/SKILL.md Trap 0.5 / "Pattern A in detail".
 */
public final class EBSharepointMsAuthAttributes implements MsAuthAttributes {

    public static final String DEFAULT_SCOPE_STRING = "openid offline_access Sites.Read.All";

    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final String email;
    private final String authType;
    private final String publicAuthMode;
    private final String routingUrl;
    private String callbackEndPoint;
    private String forwardPath;
    private String scopeString = DEFAULT_SCOPE_STRING;

    public EBSharepointMsAuthAttributes(String clientId, String clientSecret, String tenantId, String email,
                                         String authType, String routingUrl) {
        this(clientId, clientSecret, tenantId, email, authType, routingUrl, null);
    }

    public EBSharepointMsAuthAttributes(String clientId, String clientSecret, String tenantId, String email,
                                         String authType, String routingUrl, String publicAuthMode) {
        PublicCredentialStore pcs = PublicCredentialStore.getInstance();
        this.clientId = clientId == null ? pcs.getClientId() : clientId;
        this.clientSecret = clientSecret == null ? pcs.getClientSecret() : clientSecret;
        this.tenantId = tenantId;
        this.email = email;
        this.authType = authType != null ? authType : "Public";
        this.publicAuthMode = publicAuthMode != null ? publicAuthMode : "krista";
        this.routingUrl = routingUrl;
    }

    /** True when Krista has a registered multi-tenant Azure AD app for THIS deployment. */
    public static boolean isQuickSetupAvailable() {
        return PublicCredentialStore.getInstance().isLoaded();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getAuthType() {
        return authType;
    }

    public String getPublicAuthMode() {
        return publicAuthMode;
    }

    @Override
    public String getCallbackEndPoint() {
        return callbackEndPoint;
    }

    public void setCallbackEndPoint(String callbackEndPoint) {
        this.callbackEndPoint = callbackEndPoint;
    }

    @Override
    public String getForwardPath() {
        return forwardPath;
    }

    public void setForwardPath(String forwardPath) {
        this.forwardPath = forwardPath;
    }

    @Override
    public String getScopeString() {
        return scopeString;
    }

    public void setScopeString(String scopeString) {
        this.scopeString = scopeString;
    }

    public boolean isPublicAuth() {
        return "Public".equals(authType);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", clientId);
        map.put("clientSecret", clientSecret);
        map.put("tenantId", tenantId);
        map.put("email", email);
        map.put("authType", authType);
        map.put("publicAuthMode", publicAuthMode);
        map.put("scopeString", scopeString);
        map.put("forwardPath", forwardPath);
        map.put("callbackEndPoint", callbackEndPoint);
        return map;
    }

    public static EBSharepointMsAuthAttributes create(Map<String, Object> map) {
        if (map == null) {
            return new EBSharepointMsAuthAttributes(null, null, null, null, "Public", null);
        }
        EBSharepointMsAuthAttributes attrs = new EBSharepointMsAuthAttributes(
                str(map, "clientId"), str(map, "clientSecret"), str(map, "tenantId"),
                str(map, "email"), str(map, "authType"), null, str(map, "publicAuthMode"));
        if (map.get("scopeString") != null) {
            attrs.scopeString = str(map, "scopeString");
        }
        attrs.forwardPath = str(map, "forwardPath");
        attrs.callbackEndPoint = str(map, "callbackEndPoint");
        return attrs;
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : null;
    }
}
