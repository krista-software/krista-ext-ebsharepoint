package app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration;

import com.microsoft.graph.models.DriveItem;

/**
 * Parses this extension's path convention and composite-identifier convention — both chosen so the
 * reused Cloud Files domain request shapes (Source Path / Path / Source / Target: Text, Folder Id /
 * File Id: Text) work without adding a separate site/library input field to every request:
 *
 * <ul>
 *   <li><b>Path strings</b> are a full logical path: {@code /Site Name/Document Library/folder/.../file}
 *       — the document library is just the second path segment, the same way any sub-folder is, so
 *       "which document library" and "how deep to browse" are both expressed by the path alone.</li>
 *   <li><b>Folder Id / File Id</b> values are an opaque composite token {@code driveId::itemId} —
 *       a Microsoft Graph drive id already uniquely identifies a site + document library, so no
 *       separate site parameter is ever needed once an item has been resolved once.</li>
 * </ul>
 */
public final class SharePointPathResolver {

    private static final String COMPOSITE_SEPARATOR = "::";

    private SharePointPathResolver() {
    }

    public record ParsedPath(String siteName, String libraryName, String itemPath) {
    }

    /** Splits {@code /Site Name/Library/sub/path} into (siteName, libraryName, "sub/path"). */
    public static ParsedPath parse(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("A path is required, e.g. \"/Site Name/Document Library/Folder\".");
        }
        String trimmed = rawPath.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("A path is required, e.g. \"/Site Name/Document Library/Folder\".");
        }
        String[] segments = trimmed.split("/", 3);
        String siteName = segments[0];
        if (siteName.isBlank()) {
            throw new IllegalArgumentException("The path must start with a site name, e.g. \"/Site Name/Document Library\".");
        }
        String libraryName = segments.length > 1 ? segments[1] : "";
        String itemPath = segments.length > 2 ? segments[2] : "";
        return new ParsedPath(siteName, libraryName, itemPath);
    }

    /** Re-composes a parsed path back into this extension's canonical "/Site/Library[/sub/path]" form. */
    public static String normalize(ParsedPath parsed) {
        StringBuilder sb = new StringBuilder("/").append(parsed.siteName());
        if (!parsed.libraryName().isBlank()) {
            sb.append('/').append(parsed.libraryName());
        }
        if (!parsed.itemPath().isBlank()) {
            sb.append('/').append(parsed.itemPath());
        }
        return sb.toString();
    }

    public static String childPath(String parentPath, String childName) {
        String parent = parentPath == null ? "" : parentPath;
        while (parent.endsWith("/")) {
            parent = parent.substring(0, parent.length() - 1);
        }
        return parent + "/" + childName;
    }

    public static String encodeCompositeId(String driveId, String itemId) {
        if (driveId == null || driveId.isBlank() || itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Both a drive id and an item id are required to build an identifier.");
        }
        return driveId + COMPOSITE_SEPARATOR + itemId;
    }

    public record CompositeId(String driveId, String itemId) {
    }

    /**
     * Derives a human-readable absolute path for a DriveItem from its own parentReference.path (Graph
     * returns this as {@code /drives/{id}/root:/Folder/Sub} — the segment after "root:" is the
     * server-relative path) + the item's own name. Used for display only; addressing always goes
     * through the opaque driveId::itemId composite id, never this string.
     */
    public static String absolutePathOf(DriveItem item) {
        String base = "";
        if (item.parentReference != null && item.parentReference.path != null) {
            String p = item.parentReference.path;
            int idx = p.indexOf("root:");
            base = idx >= 0 ? p.substring(idx + "root:".length()) : p;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String name = item.name != null ? item.name : "";
        return base.isBlank() ? "/" + name : base + "/" + name;
    }

    public static CompositeId decodeCompositeId(String compositeId) {
        if (compositeId == null || !compositeId.contains(COMPOSITE_SEPARATOR)) {
            throw new IllegalArgumentException(
                    "Not a recognized identifier — expected a value returned by List Files, List Folders, "
                            + "or Get Folder Id by path.");
        }
        int idx = compositeId.indexOf(COMPOSITE_SEPARATOR);
        String driveId = compositeId.substring(0, idx);
        String itemId = compositeId.substring(idx + COMPOSITE_SEPARATOR.length());
        if (driveId.isBlank() || itemId.isBlank()) {
            throw new IllegalArgumentException(
                    "Not a recognized identifier — expected a value returned by List Files, List Folders, "
                            + "or Get Folder Id by path.");
        }
        return new CompositeId(driveId, itemId);
    }
}
