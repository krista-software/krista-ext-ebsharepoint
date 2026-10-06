# Get Folder Id by path

**Area:** Management · **Type:** Change

## Overview
Resolves a full folder path to the opaque Folder Id used by the folder-id based requests
([Retrieve files using folder id](pages/RetrieveFilesUsingFolderId.md),
[Get files and File Id from Folder Id](pages/GetFilesAndFileIdFromFolderId.md)). Exposed as an AI
Assistant (MCP) tool.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Path | Yes | Text | `/Site Name/Document Library/optional/sub/folder` |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Folder Id | Text | The opaque identifier for the resolved folder. |

## Error Handling
A path that resolves to a file rather than a folder, or that cannot be resolved at all, returns a
failure response with a plain-language message.
