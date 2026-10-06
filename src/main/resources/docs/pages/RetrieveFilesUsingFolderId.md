# Retrieve files using folder id

**Area:** Management · **Type:** Change

## Overview
Lists the contents of a folder identified by its opaque Folder Id (from
[Get Folder Id by path](pages/GetFolderIdByPath.md) or a prior listing), optionally filtered to items
modified on or after a date.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Folder Id | Yes | Text | The opaque identifier for the folder. |
| From Date | No | Date | Only include items modified on or after this date. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Response | [ Entity(Custom File) ] | Files and folders found in the folder (see `Is Folder`). |

## Error Handling
An unrecognized Folder Id raises an error with a plain-language message.
