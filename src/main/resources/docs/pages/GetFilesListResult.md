# Get Files List Result

**Area:** Management · **Type:** Wait For Event

## Overview
Resolves the file list produced by [List Files](pages/ListFiles.md) for the given Task ID.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Task ID | Yes | Text | The Task ID returned by List Files. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Files | [ Entity(File) ] | The files found. |
| Success | Boolean | Whether a result was found for the Task ID. |
| Message | Text | A one-line summary or an error message. |

## Error Handling
An unknown or already-retrieved Task ID returns `Success = false` with an explanatory message — each
Task ID can only be retrieved once.
