# List Folders

**Area:** Management · **Type:** Query

## Overview
Produces a non-recursive list of the folders in the folder denoted by `Source Path`. Exposed as an AI
Assistant (MCP) tool.

## Request Details
Returns a **Task ID** immediately — call [Get Folders List Result](pages/GetFoldersListResult.md) right
afterward with that Task ID to get the actual folder list.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Source Path | Yes | Text | `/Site Name/Document Library/optional/sub/folder` |
| Search | No | Text | Only include folders whose name contains this text (case-insensitive). |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Task ID | Text | Pass this to Get Folders List Result. |

## Error Handling
An unresolvable site, document library, or folder path returns a failure response with a plain-language
message instead of a Task ID.
