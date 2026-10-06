# Authentication

EB Sharepoint connects to Microsoft SharePoint Online through the Microsoft identity platform (Azure
AD). Configure the connection on the **Authentication** tab (not the plain Setup tab).

## Modes

- **User Authentication** (delegated — a specific Microsoft user signs in) — this is the single
  sign-on flow described in the original requirement. A browser popup asks the user (or admin, during
  setup) to sign in with their Microsoft work account.
- **Server to Server Authentication** (client credentials — no user sign-in; the extension authenticates
  as an application, using the whole tenant's SharePoint content without a specific signed-in user).

Both modes are available from the same Authentication tab — pick whichever matches how your
organization wants to connect.

## Quick Setup vs. Your Own Credentials

- **Quick Setup** uses a Krista-managed, multi-tenant Azure AD application — available only if Krista
  has registered one for this deployment. If this deployment has no Quick Setup credentials configured,
  the Quick Setup option will not be shown — use **Your Own Credentials** below instead.
- **Your Own Credentials** — register your own Azure AD application (see
  [Microsoft's app registration quickstart](https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app))
  and enter its Client ID / Client Secret / Tenant ID here.

## Required scopes / permissions

| Mode | Required Graph permission | Permission type | Admin consent required? |
|---|---|---|---|
| **User Authentication** (delegated — a specific user signs in) | Sites.Read.All | Delegated | Only if your tenant restricts user consent |
| **Server to Server Authentication** (client credentials — no user sign-in) | Sites.Read.All | **Application** | Always yes |

Grant only the permission type matching the mode you actually use — a Delegated grant does nothing for
a Server to Server connection, and vice versa; Azure AD evaluates these as two separate permission sets.
`Sites.Read.All` is read-only and covers everything this extension does (listing sites, document
libraries, folders and files, and downloading file content) — no write permission is requested.

## Testing the connection

Both the Authentication tab's own **Test Connection** and the Setup tab's **Test Connection** make the
same lightweight, bounded call (list up to one SharePoint site) — proving the token and the
`Sites.Read.All` scope actually work, not just that a token was issued.
