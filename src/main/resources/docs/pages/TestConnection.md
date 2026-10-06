# Test Connection

**Area:** Setup · **Type:** Query

## Overview
Verifies that this extension can reach Microsoft SharePoint using the credentials already saved on the
Authentication tab. Exposed as an AI Assistant (MCP) tool.

## Input Parameters
None.

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Success | Boolean | Whether the connection succeeded. |
| Message | Text | "Connected to Microsoft SharePoint." or a friendly error message. |

## Error Handling
If nothing has been configured yet, run Test Connection from the Authentication tab first. Graph/MSAL
errors are translated into plain-language messages (invalid client id/secret, tenant not found, consent
required, access denied).
