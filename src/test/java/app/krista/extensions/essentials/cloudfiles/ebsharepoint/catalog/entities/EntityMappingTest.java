package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities;

import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.ItemReference;
import com.microsoft.graph.models.Shared;
import com.microsoft.graph.models.Site;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Pure-Java transformer tests: Graph model POJOs in, entity POJOs out — no Krista runtime involved. */
class EntityMappingTest {

    private static DriveItem baseFile() {
        DriveItem item = new DriveItem();
        item.id = "item-1";
        item.name = "report.docx";
        item.size = 2048L;
        item.createdDateTime = OffsetDateTime.parse("2026-01-15T10:00:00Z");
        item.lastModifiedDateTime = OffsetDateTime.parse("2026-02-01T09:30:00Z");
        ItemReference ref = new ItemReference();
        ref.path = "/drives/drive-1/root:/Shared Documents/Reports";
        item.parentReference = ref;
        IdentitySet createdBy = new IdentitySet();
        Identity creator = new Identity();
        creator.displayName = "Alex Smith";
        createdBy.user = creator;
        item.createdBy = createdBy;
        return item;
    }

    @Test
    void fileEntityMapsCoreFields() {
        DriveItem item = baseFile();
        FileEntity entity = FileEntity.fromGraph(item, "drive-1");

        assertEquals("report.docx", entity.name);
        assertEquals("report", entity.baseName);
        assertEquals("docx", entity.extension);
        assertEquals("/Shared Documents/Reports/report.docx", entity.absolutePath);
        assertEquals("/Shared Documents/Reports", entity.parentFolder);
        assertEquals("Alex Smith", entity.owner);
        assertEquals(2048.0, entity.size);
        assertFalse(entity.isFolder);
        assertEquals("Private", entity.permissions);
        assertNull(entity.file);
    }

    @Test
    void fileEntityDetectsSharedPermission() {
        DriveItem item = baseFile();
        item.shared = new Shared();
        FileEntity entity = FileEntity.fromGraph(item, "drive-1");
        assertEquals("Shared", entity.permissions);
    }

    @Test
    void folderEntityMapsFolderIdAndFlag() {
        DriveItem item = baseFile();
        item.name = "Reports";
        item.folder = new Folder();
        item.folder.childCount = 3;

        FolderEntity entity = FolderEntity.fromGraph(item, "drive-1");

        assertTrue(entity.isFolder);
        assertEquals("drive-1::item-1", entity.folderId);
        assertEquals("Reports", entity.name);
    }

    @Test
    void customFileEntityEncodesFileIdAndFlag() {
        DriveItem fileItem = baseFile();
        CustomFileEntity fileEntity = CustomFileEntity.fromGraph(fileItem, "drive-1");
        assertFalse(fileEntity.isFolder);
        assertEquals("drive-1::item-1", fileEntity.fileId);

        DriveItem folderItem = baseFile();
        folderItem.folder = new Folder();
        CustomFileEntity folderEntity = CustomFileEntity.fromGraph(folderItem, "drive-1");
        assertTrue(folderEntity.isFolder);
    }

    @Test
    void siteEntityMapsDisplayFields() {
        Site site = new Site();
        site.name = "marketing";
        site.displayName = "Marketing Site";
        site.webUrl = "https://contoso.sharepoint.com/sites/marketing";
        site.description = "Marketing team site";
        site.createdDateTime = OffsetDateTime.parse("2025-06-01T00:00:00Z");
        site.lastModifiedDateTime = OffsetDateTime.parse("2026-01-01T00:00:00Z");

        SiteEntity entity = SiteEntity.fromGraph(site);

        assertEquals("marketing", entity.name);
        assertEquals("Marketing Site", entity.displayName);
        assertEquals("https://contoso.sharepoint.com/sites/marketing", entity.webUrl);
        assertNotNull(entity.created);
        assertNotNull(entity.modified);
    }
}
