# Entities

## Site
A Microsoft SharePoint site.

| Field | Type | Description |
|---|---|---|
| Name | Text | The site's internal (URL-safe) name. |
| Display Name | Text | The site's human-readable display name. |
| Web Url | Text | The site's browser URL. |
| Description | RichText | The site's description, if set. |
| Created | Date | When the site was created. |
| Modified | Date | When the site was last modified. |

## File
A file within a SharePoint document library (reused from the existing Cloud Files domain shape).

| Field | Type | Description |
|---|---|---|
| Name | Text | File name including extension. |
| Base Name | Text | File name without extension. |
| Extension | Text | File extension. |
| Parent Folder | Text | The containing folder's path. |
| Absolute Path | Text | The file's full path. |
| Owner | Text | Display name of the account that created the file. |
| Created | Date | Creation time. |
| Modified | Date | Last modification time. |
| Size | Number | Size in bytes. |
| Permissions | PickOne (Shared / Private) | Best-effort: whether the file has been shared with anyone. |
| Is Folder | Boolean | Always `false` for this entity. |
| Tags | [ Text ] | Not populated by this build (Graph's children listing has no tags). |
| Shared With | [ Text ] | Not populated by this build. |
| Created By | Text | Same as Owner. |
| Modified By | Text | Display name of the account that last modified the file. |
| File | File | Not populated by list operations — use Download as File / Download File using the File Id. |

## Folder
A folder within a SharePoint document library (reused from the existing Cloud Files domain shape).

| Field | Type | Description |
|---|---|---|
| Name | Text | Folder name. |
| Base Name | Text | Same as Name (folders have no extension). |
| Extension | Text | Empty for folders. |
| Parent Folder | Text | The containing folder's path. |
| Absolute Path | Text | The folder's full path. |
| Owner | Text | Display name of the account that created the folder. |
| Created | Date | Creation time. |
| Modified | Date | Last modification time. |
| Size | Number | Size in bytes (sum of contents, per Graph). |
| Permissions | Text | Best-effort: "Shared" or "Private". |
| Is Folder | Boolean | Always `true` for this entity. |
| Folder ID | Text | Opaque identifier usable by the folder-id requests. |
| Tags | [ Text ] | Not populated by this build. |
| Created By | Text | Same as Owner. |
| Modified By | Text | Display name of the account that last modified the folder. |
| File | File | Not populated — folders have no content to download. |
| Shared With | [ Text ] | Not populated by this build. |

## Custom File
A generic file-or-folder listing item, reused verbatim from the existing Cloud Files domain entity of
the same name — used by the folder-id/path listing requests that don't distinguish files from folders
in their own naming (`Retrieve files using folder id`, `Get files and File Id from Folder Id`,
`Fetch Files From Folder`). Use `Is Folder` to tell which.

| Field | Type | Description |
|---|---|---|
| Name | Text | File or folder name. |
| Base Name | Text | Name without extension. |
| Extension | Text | Extension, empty for folders. |
| File Id | Text | Opaque identifier usable by Download File using the File Id. |
| Created | Date | Creation time. |
| Size | Number | Size in bytes. |
| Absolute Path | Text | Full path. |
| Parent Folder | Text | The containing folder's path. |
| Is Folder | Boolean | Whether this entry is a folder. |
