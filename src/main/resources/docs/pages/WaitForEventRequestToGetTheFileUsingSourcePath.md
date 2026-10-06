# Wait For Event Request to get the file using source path

**Area:** Management · **Type:** Query

## Overview
An alternate, Task-ID-shaped way to download a file by path — schedules the download and returns a
Task Id, resolved by [Get the Downloaded file](pages/GetTheDownloadedFile.md). Prefer
[Download as File](pages/DownloadAsFile.md) for a simpler one-step download; use this pair if your
workflow prefers the Task Id / Wait-For-Event shape used elsewhere in this extension.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Source Path | Yes | Text | `/Site Name/Document Library/folder/file.ext` |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Task Id | Text | Pass this to Get the Downloaded file. |

## Error Handling
Pointing `Source Path` at a folder instead of a file, or an unresolvable path, returns a failure
response instead of a Task Id.
