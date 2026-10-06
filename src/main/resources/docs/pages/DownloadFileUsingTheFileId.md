# Download File using the File Id

**Area:** Management · **Type:** Change

## Overview
Downloads a file's content using the opaque File Id returned by [List Files](pages/ListFiles.md) /
[Get Files List Result](pages/GetFilesListResult.md) (or any other request that returns a File/Custom
File entity). Exposed as an AI Assistant (MCP) tool.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| File Id | Yes | Text | The opaque identifier from a prior listing request — not a path. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| File | File | The downloaded file. |

## Error Handling
An unrecognized File Id, or an id that points to a folder, raises an error with a plain-language
message.
