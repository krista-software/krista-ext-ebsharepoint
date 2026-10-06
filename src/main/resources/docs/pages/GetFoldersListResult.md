# Get Folders List Result

**Area:** Management · **Type:** Wait For Event

## Overview
Resolves the folder list produced by [List Folders](pages/ListFolders.md) for the given Task ID.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Task ID | Yes | Text | The Task ID returned by List Folders. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Folders | [ Entity(Folder) ] | The folders found. |
| Success | Boolean | Whether a result was found for the Task ID. |
| Message | Text | A one-line summary or an error message. |

## Error Handling
An unknown or already-retrieved Task ID returns `Success = false` with an explanatory message.
