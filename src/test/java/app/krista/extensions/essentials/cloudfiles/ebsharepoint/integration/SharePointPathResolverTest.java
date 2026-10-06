package app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration;

import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ItemReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharePointPathResolverTest {

    @Test
    void parsesSiteLibraryAndSubPath() {
        SharePointPathResolver.ParsedPath p = SharePointPathResolver.parse("/Marketing Site/Shared Documents/Reports/2026");
        assertEquals("Marketing Site", p.siteName());
        assertEquals("Shared Documents", p.libraryName());
        assertEquals("Reports/2026", p.itemPath());
    }

    @Test
    void parsesSiteAndLibraryOnly() {
        SharePointPathResolver.ParsedPath p = SharePointPathResolver.parse("/Marketing Site/Shared Documents");
        assertEquals("Marketing Site", p.siteName());
        assertEquals("Shared Documents", p.libraryName());
        assertEquals("", p.itemPath());
    }

    @Test
    void tolerantOfLeadingTrailingSlashesAndWhitespace() {
        SharePointPathResolver.ParsedPath p = SharePointPathResolver.parse("  /Site/Lib/  ");
        assertEquals("Site", p.siteName());
        assertEquals("Lib", p.libraryName());
    }

    @Test
    void blankPathRejected() {
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.parse(""));
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.parse(null));
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.parse("///"));
    }

    @Test
    void normalizeRecomposesCanonicalPath() {
        SharePointPathResolver.ParsedPath p = new SharePointPathResolver.ParsedPath("Site", "Lib", "Sub/Folder");
        assertEquals("/Site/Lib/Sub/Folder", SharePointPathResolver.normalize(p));
    }

    @Test
    void normalizeOmitsEmptySegments() {
        SharePointPathResolver.ParsedPath p = new SharePointPathResolver.ParsedPath("Site", "", "");
        assertEquals("/Site", SharePointPathResolver.normalize(p));
    }

    @Test
    void childPathAppendsNameWithoutDoubleSlash() {
        assertEquals("/Site/Lib/child.txt", SharePointPathResolver.childPath("/Site/Lib/", "child.txt"));
        assertEquals("/Site/Lib/child.txt", SharePointPathResolver.childPath("/Site/Lib", "child.txt"));
    }

    @Test
    void compositeIdRoundTrips() {
        String id = SharePointPathResolver.encodeCompositeId("drive123", "item456");
        SharePointPathResolver.CompositeId decoded = SharePointPathResolver.decodeCompositeId(id);
        assertEquals("drive123", decoded.driveId());
        assertEquals("item456", decoded.itemId());
    }

    @Test
    void encodeCompositeIdRejectsBlankParts() {
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.encodeCompositeId("", "x"));
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.encodeCompositeId("x", null));
    }

    @Test
    void decodeCompositeIdRejectsUnrecognizedValue() {
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.decodeCompositeId("not-a-composite-id"));
        assertThrows(IllegalArgumentException.class, () -> SharePointPathResolver.decodeCompositeId(null));
    }

    @Test
    void absolutePathOfUsesParentReferencePathAfterRootMarker() {
        DriveItem item = new DriveItem();
        item.name = "report.docx";
        ItemReference ref = new ItemReference();
        ref.path = "/drives/abc/root:/Shared Documents/Reports";
        item.parentReference = ref;

        assertEquals("/Shared Documents/Reports/report.docx", SharePointPathResolver.absolutePathOf(item));
    }

    @Test
    void absolutePathOfFallsBackToRootWhenNoParentReference() {
        DriveItem item = new DriveItem();
        item.name = "report.docx";
        assertEquals("/report.docx", SharePointPathResolver.absolutePathOf(item));
    }
}
