# List Files

**Area:** Management · **Type:** Query

## Overview
Produces a non-recursive list of the files in the folder denoted by `Source Path`. Exposed as an AI
Assistant (MCP) tool.

## Request Details
Returns a **Task ID** immediately — call [Get Files List Result](pages/GetFilesListResult.md) right
afterward with that Task ID to get the actual file list (see [Overview](pages/overview.md)'s note on
async-shaped requests).

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Source Path | Yes | Text | `/Site Name/Document Library/optional/sub/folder` |
| Search | No | Text | Only include files whose name contains this text (case-insensitive). |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Task ID | Text | Pass this to Get Files List Result. |

## Error Handling
An unresolvable site, document library, or folder path returns a failure response with a plain-language
message instead of a Task ID.
