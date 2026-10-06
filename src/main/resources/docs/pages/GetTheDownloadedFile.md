# Get the Downloaded file

**Area:** Management · **Type:** Wait For Event

## Overview
Resolves the file downloaded by [Wait For Event Request to get the file using source path](pages/WaitForEventRequestToGetTheFileUsingSourcePath.md)
for the given Task Id.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Task Id | Yes | Text | The Task Id returned by Wait For Event Request to get the file using source path. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| File | File | The downloaded file. |
| Success | Boolean | Whether a result was found for the Task Id. |
| Message | Text | A one-line summary or an error message. |

## Error Handling
An unknown or already-retrieved Task Id returns `Success = false` with an explanatory message.
