# Overview

EB Sharepoint connects Krista to Microsoft SharePoint Online so workflows can browse and retrieve
content from your organization's SharePoint sites.

## What it covers

- **List all sites** your connected account can see ([List User Sites](pages/ListUserSites.md)).
- **List files and folders** inside a site's document library, at any sub-folder depth
  ([List Files](pages/ListFiles.md), [List Folders](pages/ListFolders.md), plus the folder-id/path
  variants under [Supported requests](pages/supported-requests.md)).
- **Download a file's contents** as a Krista File, either by its full path or by the opaque identifier
  a listing request returned ([Download as File](pages/DownloadAsFile.md),
  [Download File using the File Id](pages/DownloadFileUsingTheFileId.md)).

## Identifying things: paths and ids

Every "path" field in this extension (`Source Path`, `Path`, `Source`, `Target`) is a single string of
the form:

```
/Site Name/Document Library/optional/sub/folder/file.ext
```

The document library (e.g. "Shared Documents") is just the second path segment — the same way any
sub-folder is — so one path string is enough to reach any site, any document library, and any folder
depth, without a separate site or library selector field.

`Folder Id` and `File Id` values are opaque tokens returned by the listing and resolve requests (e.g.
[Get Folder Id by path](pages/GetFolderIdByPath.md)) — pass them back exactly as returned to the
folder-id/file-id based requests. Don't try to construct or parse one by hand.

## Authentication

See [Authentication](pages/Authentication.md) — this extension signs in with a Microsoft work account
(single sign-on) or a Server-to-Server (client-credentials) connection, configured on the
**Authentication** tab.

## Async-shaped requests

A few requests (`List Files`, `List Folders`, and the source-path download pair) return a **Task ID**
rather than the result directly, matching the existing Cloud Files domain's established shape for these
operations. In this extension the underlying SharePoint call already completed by the time the Task ID
comes back — call the paired "Get ... Result" / "Get the Downloaded file" request immediately afterward
to retrieve it; there is no real waiting involved.
