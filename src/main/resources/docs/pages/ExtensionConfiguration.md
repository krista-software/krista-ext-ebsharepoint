# Extension Configuration

EB Sharepoint has no Setup-tab connection fields of its own — all configuration happens on the
**Authentication** tab (see [Authentication](pages/Authentication.md)), per Krista's standard pattern
for Microsoft-product extensions.

| Field | Required | Default | Description |
|---|---|---|---|
| *(none — see Authentication tab)* | — | — | — |

## Setup tab

The Setup tab's own **Test Connection** button checks the credentials already saved on the
Authentication tab. If nothing has been configured yet, it reports "Not configured — please
authenticate via the Authentication tab first."

**Health Check** reports whether SharePoint is currently reachable with the saved credentials, plus
basic JVM heap usage.
