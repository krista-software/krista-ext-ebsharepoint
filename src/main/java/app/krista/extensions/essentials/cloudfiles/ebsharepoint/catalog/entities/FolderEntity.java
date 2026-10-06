package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.SharePointPathResolver;
import com.microsoft.graph.models.DriveItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A SharePoint folder — reused verbatim from the existing Cloud Files domain entity "Folder"
 * (localDomainEntity_f6abaeea-...). The "File" field (native binary) and "Tags"/"Shared With" are left
 * unpopulated by this build's list operations (Graph's children listing doesn't include tags or a full
 * sharing-recipient list without an extra per-item call) — see docs/pages/entities.md.
 */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
@Entity(name = "Folder",
        id = "localDomainEntity_f6abaeea-5400-4cc7-9ec4-e884e723620c",
        primaryKey = "Folder ID",
        supportStore = false,
        description = "A folder within a SharePoint document library, reused from the existing Cloud Files domain shape.")
public class FolderEntity {

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "Base Name", required = false)
    public String baseName;

    @Field.Text(name = "Extension", required = false)
    public String extension;

    @Field.Text(name = "Parent Folder", required = false)
    public String parentFolder;

    @Field.Text(name = "Absolute Path", required = false)
    public String absolutePath;

    @Field.Text(name = "Owner", required = false, description = "Display name of the account that created the folder.")
    public String owner;

    @Field.Date(name = "Created", required = false)
    public Long created;

    @Field.Date(name = "Modified", required = false)
    public Long modified;

    @Field(name = "Size", type = "Number", required = false)
    public Double size;

    @Field.Text(name = "Permissions", required = false, description = "Best-effort: \"Shared\" or \"Private\".")
    public String permissions;

    @Field.Boolean(name = "Is Folder", required = false)
    public Boolean isFolder;

    @Field.Text(name = "Folder ID", required = false, description = "Opaque identifier (driveId::itemId) usable by the folder-id requests.")
    public String folderId;

    @Field.Desc(name = "Tags", type = "[ Text ]", required = false)
    public List<String> tags;

    @Field.Text(name = "Created By", required = false)
    public String createdBy;

    @Field.Text(name = "Modified By", required = false)
    public String modifiedBy;

    @Field.File(name = "File", required = false, description = "Not populated by list operations — use Download as File / Download File using the File Id.")
    public app.krista.model.base.File file;

    @Field.Desc(name = "Shared With", type = "[ Text ]", required = false)
    public List<String> sharedWith;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Name", name);
        fields.put("Base Name", baseName);
        fields.put("Extension", extension);
        fields.put("Parent Folder", parentFolder);
        fields.put("Absolute Path", absolutePath);
        fields.put("Owner", owner);
        fields.put("Created", created);
        fields.put("Modified", modified);
        fields.put("Size", size);
        fields.put("Permissions", permissions);
        fields.put("Is Folder", isFolder);
        fields.put("Folder ID", folderId);
        fields.put("Tags", tags);
        fields.put("Created By", createdBy);
        fields.put("Modified By", modifiedBy);
        fields.put("File", file);
        fields.put("Shared With", sharedWith);
        return fields;
    }

    public static FolderEntity fromGraph(DriveItem item, String driveId) {
        FolderEntity entity = new FolderEntity();
        entity.name = item.name;
        int dot = item.name != null ? item.name.lastIndexOf('.') : -1;
        entity.baseName = dot > 0 ? item.name.substring(0, dot) : item.name;
        entity.extension = dot > 0 ? item.name.substring(dot + 1) : "";
        String absolutePath = SharePointPathResolver.absolutePathOf(item);
        entity.parentFolder = parentOf(absolutePath);
        entity.absolutePath = absolutePath;
        entity.owner = item.createdBy != null && item.createdBy.user != null ? item.createdBy.user.displayName : null;
        entity.created = item.createdDateTime != null ? item.createdDateTime.toInstant().toEpochMilli() : null;
        entity.modified = item.lastModifiedDateTime != null ? item.lastModifiedDateTime.toInstant().toEpochMilli() : null;
        entity.size = item.size != null ? item.size.doubleValue() : null;
        entity.permissions = item.shared != null ? "Shared" : "Private";
        entity.isFolder = item.folder != null;
        entity.folderId = SharePointPathResolver.encodeCompositeId(driveId, item.id);
        entity.tags = List.of();
        entity.createdBy = entity.owner;
        entity.modifiedBy = item.lastModifiedBy != null && item.lastModifiedBy.user != null
                ? item.lastModifiedBy.user.displayName : null;
        entity.file = null;
        entity.sharedWith = List.of();
        return entity;
    }

    private static String parentOf(String absolutePath) {
        if (absolutePath == null) {
            return "/";
        }
        int lastSlash = absolutePath.lastIndexOf('/');
        return lastSlash > 0 ? absolutePath.substring(0, lastSlash) : "/";
    }
}
