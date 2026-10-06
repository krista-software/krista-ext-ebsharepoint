# Download as File

**Area:** Management · **Type:** Change

## Overview
Downloads a file's content, identified by its full path, and returns it as a Krista File. Exposed as an
AI Assistant (MCP) tool.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Source | Yes | Text | `/Site Name/Document Library/folder/file.ext` |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Krista File | File | The downloaded file. |
| Success | Boolean | Whether the download succeeded. |
| Message | RichText | A one-line summary or an error message. |

## Error Handling
Pointing `Source` at a folder instead of a file, or an unresolvable path, returns `Success = false` with
a plain-language message rather than throwing.
