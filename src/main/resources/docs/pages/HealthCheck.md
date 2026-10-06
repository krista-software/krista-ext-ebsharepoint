# Health Check

**Area:** Setup · **Type:** Change

## Overview
Reports whether SharePoint is currently reachable, plus basic JVM heap usage for this extension.
Exposed as an AI Assistant (MCP) tool.

## Input Parameters
None.

## Output Parameters
| Field | Type | Description |
|---|---|---|
| Is Healthy | Boolean | Whether SharePoint connectivity succeeded. |
| Heap Used MB | Number | Current JVM heap usage in megabytes. |
| Details | Text | "OK — SharePoint reachable." or a friendly error message. |
