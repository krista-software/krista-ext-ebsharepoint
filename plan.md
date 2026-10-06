# EB Sharepoint — Implementation Plan

## Summary
- **Title / name**: EB Sharepoint (`ebsharepoint`), version `1.0.0`
- **Package**: `app.krista.extensions.essentials.cloudfiles.ebsharepoint`
- **Ecosystem / Domain**: Essentials / Cloud Files
- **Provider**: Microsoft SharePoint Online, via Microsoft Graph API v1.0
- **Capabilities**: file handling (binary download) only — no entity search, webhooks, MCP, or Solutions tab for this build.

## Authentication
Wizard answer: `oauth2-client-credentials,oauth2-interactive`. Because SharePoint is a Microsoft product, **Krista pattern D (microsoft-auth-sdk) always applies regardless of that answer** — this is a non-negotiable rule, not a conflict. The good news: pattern D's `AuthFlow` model supports both mechanisms the admin picked, as two selectable modes on the same Authentication tab (per `adding-krista-msauth-tabs`, one tab, `supportedFlows` lists both):

| Mode | AuthFlow | Matches wizard answer |
|---|---|---|
| User Authentication (delegated sign-in — this is the "single sign-on" the requirement asked for) | `AUTH_CODE_PUBLIC` / `AUTH_CODE_PRIVATE` | `oauth2-interactive` |
| Server to Server (app-only) | `CLIENT_CREDENTIALS_SECRET` | `oauth2-client-credentials` |

Both modes go through the same Authentication tab (Pattern A hand-rolled resource per the msauth skill); the admin picks one per connection. `useSetupEmail=true` is used consistently for the single-mailbox/operator case (no per-end-user delegation was requested).

## Domain reuse
Essentials/Cloud Files already had a full set of generic file-storage entities and requests (this domain has clearly accumulated requests from prior cloud-storage extensions — Dropbox/OneDrive/Box-style). Per your review:

- **Reused verbatim (13 items)**: entities `Custom File`, `Folder`, `File`; requests `List Files`, `List Folders`, `List User Sites`, `Download as File`, `Download File using the File Id`, `Get the Downloaded file`, `Retrieve files using folder id`, `Get files and File Id from Folder Id`, `Fetch Files From Folder`, `Get Folder Id by path`. Same titles, same input/output field names as the existing catalog entries — implemented here against SharePoint/Graph.
- **New**: entity `Site` (Cloud Files had no site-level entity to reuse).
- **New (plumbing, not part of your review batch — flagged for your attention)**: `Get Files List Result`, `Get Folders List Result`, and `Wait For Event Request to get the file using source path`. These exist purely to resolve the Task ID outputs that the approved `List Files`/`List Folders`/`Get the Downloaded file` reuses require — without them those three approved requests would have no way to actually return data. They're modeled on sibling items already present in the domain catalog (`Get Result`, `Get Response`, and an identically-named existing request) rather than invented from scratch, but since they weren't in your original review list, please confirm they're wanted as-is, or tell me to simplify the download/listing flow to be fully synchronous instead (dropping the Task ID indirection).
- **Nothing was avoided.**

**Path/identifier convention** (to make the reused shapes work without a dedicated site/library input field, consistent with the Cloud Files domain's existing path-string style used by `Source`/`Target`/`Path` fields domain-wide): every `Source Path` / `Path` / `Source` / `Target` string is a full logical path of the form `/{Site Name}/{Document Library Name}/{folder}/.../{file}`. This is how "list all sites", "browse into any sub-folder" (your folder-browsing-depth answer), and "target a specific document library" (your multiple-document-libraries answer) are all satisfied without extra input fields: the library is just a path segment, same as any folder. `Folder Id`/`File Id` values are opaque composite tokens (`siteId:itemId`) returned by the list/resolve requests, so downstream by-ID requests never need a separate site parameter.

## Setup

| Request | Inputs | Output | External endpoint |
|---|---|---|---|
| Test Connection | — | Text | `GET /sites?search=*` (probe) |
| Health Check | — | Text | `GET /sites?search=*` |

## Area: Operations

| Request | Inputs | Output entity | External endpoint | Reuse |
|---|---|---|---|---|
| List User Sites | — | `[ Entity(Site) ]` | `GET /sites?search=*` | existing (`localDomainRequest_f32f165f…`) |

## Area: Management

| Request | Inputs | Output | External endpoint | Reuse |
|---|---|---|---|---|
| List Files | Source Path (req), Search (opt) | Text (Task ID) | `GET /sites/{siteId}/drive/root:/{path}:/children` | existing (`localDomainRequest_f5f0a870…`) |
| Get Files List Result | Task ID (req) | `[ Entity(File) ]` | internal — resolves buffered children, file items only | new (plumbing) |
| List Folders | Source Path (req), Search (opt) | Text (Task ID) | `GET /sites/{siteId}/drive/root:/{path}:/children` | existing (`localDomainRequest_3f16a853…`) |
| Get Folders List Result | Task ID (req) | `[ Entity(Folder) ]` | internal — resolves buffered children, folder items only | new (plumbing) |
| Download as File | Source (req) | File (binary) + Switch + RichText | `GET /sites/{siteId}/drive/root:/{path}:/content` | existing (`localDomainRequest_d0aea3ac…`) |
| Download File using the File Id | File Id (req) | File (binary) | `GET /sites/{siteId}/drive/items/{itemId}/content` | existing (`localDomainRequest_4e95bf64…`) |
| Wait For Event Request to get the file using source path | Source Path (req) | Text (Task Id) | internal — mints Task Id for async download | new (plumbing) |
| Get the Downloaded file | Task Id (req) | File (binary) + Switch + Text | internal — resolves buffered download | existing (`localDomainRequest_a151c209…`) |
| Retrieve files using folder id | Folder Id (req), From Date (opt) | `[ Entity(Custom File) ]` | `GET /sites/{siteId}/drive/items/{folderId}/children?$filter=...` | existing (`localDomainRequest_fcefb078…`) |
| Get files and File Id from Folder Id | Folder Id (req), From Date (opt) | `[ Entity(Custom File) ]` | `GET /sites/{siteId}/drive/items/{folderId}/children?$filter=...` | existing (`localDomainRequest_a5e85205…`) |
| Fetch Files From Folder | Path (req), From Date (opt) | `[ Entity(Custom File) ]` | `GET /sites/{siteId}/drive/root:/{path}:/children?$filter=...` | existing (`localDomainRequest_7f15df55…`) |
| Get Folder Id by path | Path (req) | Text (Folder Id) | `GET /sites/{siteId}/drive/root:/{path}:` | existing (`localDomainRequest_3513c211…`) |

## Entities

| Entity | Fields | Reuse |
|---|---|---|
| Site | Name, Display Name, Web Url, Description, Created, Modified | new |
| File | Name, Base Name, Extension, Parent Folder, Absolute Path, Owner, Created, Modified, Size, Permissions, Is Folder, Tags, Shared With, Created By, Modified By, File | existing (`localDomainEntity_292c2a1d…`) |
| Folder | Name, Base Name, Extension, Parent Folder, Absolute Path, Owner, Created, Modified, Size, Permissions, Is Folder, Folder ID, Tags, Created By, Modified By, File, Shared With | existing (`localDomainEntity_f6abaeea…`) |
| Custom File | Name, Base Name, Extension, File Id, Created, Size, Absolute Path, Parent Folder, Is Folder | existing (`localDomainEntity_3e0f56dd…`) |

## Capabilities
Entity search: **no** · Webhooks: **no** · MCP: **no** · Solutions tab: **no** · Binary file handling: **yes** (downloads use `KristaMediaClient`/`FileRepository` per the business-logic skill, not raw bytes).

## Placeholder IDs — must replace before deploy
- `@Domain` id, `ecosystemId`, and `ecosystemVersion` for Essentials/Cloud Files — `domain-catalog.json` confirmed the ecosystem/domain *exist* and listed their published entities/requests, but did not supply the real registered `@Domain`/`ecosystemId` GUIDs. Stable placeholder UUIDs will be minted so the code compiles, but **these must be swapped for the real registered values before this extension can bind** — copy them from any sibling extension already published in Essentials/Cloud Files.
- No other placeholders are expected (Azure AD app registration is the customer's own setup step via the Authentication tab, not a build-time placeholder).

## Open item for your confirmation
The three "new (plumbing)" requests above (`Get Files List Result`, `Get Folders List Result`, `Wait For Event Request to get the file using source path`) are needed to make the reused Task-ID-based requests functional, but weren't part of your original per-item review. Let me know if you'd like them kept as planned, or if you'd rather I flatten `List Files`/`List Folders`/the download flow into direct synchronous responses instead (simpler, but diverges from the reused request shapes' exact output field names).
