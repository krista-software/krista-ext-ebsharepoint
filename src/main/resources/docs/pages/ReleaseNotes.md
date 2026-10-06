# Release notes

## Version 1.0.0 (October 2026)

Initial release.

### New Features
- List all SharePoint sites the connection can see.
- List files and folders within a site's document library, at any sub-folder depth.
- Download a file's content by path or by a previously-returned File Id.
- Single sign-on (delegated) and Server-to-Server (client-credentials) authentication, configured on
  the Authentication tab.

### Resolved Bugs
Not Available.

### Dependency Updates
- Krista Service APIs (Java): 1.0.125-sp1
- microsoft-auth-sdk: 1.0.8
- microsoft-graph: 5.80.0

### Known Issues
- `Tags` and `Shared With` are not populated on File/Folder entities — Graph's folder-listing call
  does not return these without an additional per-item request.

### Limitations
- Read-only: this extension lists and downloads content; it does not create, modify, or delete
  anything in SharePoint.
