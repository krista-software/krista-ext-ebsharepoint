# Get files and File Id from Folder Id

**Area:** Management · **Type:** Change

## Overview
Same as [Retrieve files using folder id](pages/RetrieveFilesUsingFolderId.md) — lists the contents of a
folder identified by its opaque Folder Id, optionally filtered by modification date. Kept as a separate
request to match the existing Cloud Files domain's established naming for this operation.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Folder Id | Yes | Text | The opaque identifier for the folder. |
| From Date | No | Date | Only include items modified on or after this date. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| List of Files | [ Entity(Custom File) ] | Files and folders found in the folder (see `Is Folder`). |

## Error Handling
An unrecognized Folder Id raises an error with a plain-language message.
