package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.sdk.msauth.config.AuthFlow;
import app.krista.sdk.msauth.extension.annotations.AuditConfig;
import app.krista.sdk.msauth.extension.annotations.AuthScope;
import app.krista.sdk.msauth.extension.annotations.MsAuthExtension;

/**
 * Microsoft-auth-SDK declarative config (Pattern D). Supports both mechanisms the project's
 * authentication answer named: interactive delegated sign-in (the "single sign-on" the requirement
 * asked for) and Server-to-Server client-credentials — one Authentication tab, both AuthFlow values in
 * supportedFlows (see adding-krista-msauth-tabs/SKILL.md, Trap 0's note on multiple mechanisms).
 */
@MsAuthExtension(
        extensionType = "ebsharepoint",
        callbackPathPrefix = "/ebsharepoint",
        supportedFlows = {AuthFlow.AUTH_CODE_PUBLIC, AuthFlow.AUTH_CODE_PRIVATE, AuthFlow.CLIENT_CREDENTIALS_SECRET},
        defaultScopes = {"openid", "offline_access", "Sites.Read.All"},
        headerTitle = "EB Sharepoint Authentication",
        headerSubtitle = "Configure how Krista connects to your Microsoft SharePoint tenant",
        showScopePanel = true,
        supportsOwnCredentials = true,
        enableAuditDashboard = true,
        setupGuideUrl = "https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app"
)
// Scope attributed to exactly the catalog requests whose Graph calls need it — Test Connection /
// Health Check call `.sites()` (read), and every real business request under Management/Operations
// reads sites/drives/items. Sites.Read.All covers read-only site, drive and file metadata/content
// access, matching this build's read-only scope (list/download only, no writes).
@AuthScope(value = "Sites.Read.All",
        description = "Read SharePoint sites, document libraries, folders and files",
        required = true,
        catalogRequests = {"Test Connection", "Health Check", "List User Sites", "List Files", "List Folders",
                "Download as File", "Download File using the File Id", "Retrieve files using folder id",
                "Get files and File Id from Folder Id", "Fetch Files From Folder", "Get Folder Id by path"})
@AuditConfig(
        dashboardTitle = "EB Sharepoint Audit Dashboard",
        enableCredentialHealth = true,
        enableCharts = true,
        csvExportPrefix = "ebsharepoint-audit"
)
public class EBSharepointAuthConfig {
}
