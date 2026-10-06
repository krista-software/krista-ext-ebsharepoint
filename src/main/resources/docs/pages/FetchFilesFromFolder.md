# Fetch Files From Folder

**Area:** Management · **Type:** Change

## Overview
Lists the contents of a folder identified by its full path, optionally filtered to items modified on or
after a date.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Path | Yes | Text | `/Site Name/Document Library/optional/sub/folder` |
| From Date | No | Date | Only include items modified on or after this date. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Response | [ Entity(Custom File) ] | Files and folders found in the folder (see `Is Folder`). |

## Error Handling
An unresolvable site, document library, or folder path raises an error with a plain-language message.
