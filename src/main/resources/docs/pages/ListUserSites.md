# List User Sites

**Area:** Operations · **Type:** Query

## Overview
Retrieves every SharePoint site the connected account can see. Fulfils the "list all sites"
requirement. Exposed as an AI Assistant (MCP) tool.

## Request Details
Microsoft Graph's site listing is a search endpoint — an empty `Search` field automatically searches
with Microsoft's own `*` wildcard, so you get every visible site by default.

## Input Parameters
| Field | Required | Type | Description |
|---|---|---|---|
| Search | No | Text | Filter sites by name. Leave blank to list every site. |

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Sites | [ Entity(Site) ] | The matching sites. |
| Message | Text | A one-line summary (e.g. "Found 12 site(s).") or an error message. |
| Success | Boolean | Whether the call succeeded. |

## Example
Leave `Search` blank to list every site in the tenant; enter part of a site name (e.g. "Marketing") to
narrow the results.

## Error Handling
A connectivity or permission failure is translated into a plain-language message and returned with
`Success = false` rather than throwing.
