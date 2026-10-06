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
 * A SharePoint file — reused verbatim from the existing Cloud Files domain entity "File"
 * (localDomainEntity_292c2a1d-...). The "File" field (native binary) and "Tags"/"Shared With" are left
 * unpopulated by list operations — use Download as File / Download File using the File Id for content.
 */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
@Entity(name = "File",
        id = "localDomainEntity_292c2a1d-0d6c-4307-a4d1-04176dd5a315",
        primaryKey = "Absolute Path",
        supportStore = false,
        description = "A file within a SharePoint document library, reused from the existing Cloud Files domain shape.")
public class FileEntity {

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

    @Field.Text(name = "Owner", required = false, description = "Display name of the account that created the file.")
    public String owner;

    @Field.Date(name = "Created", required = false)
    public Long created;

    @Field.Date(name = "Modified", required = false)
    public Long modified;

    @Field(name = "Size", type = "Number", required = false)
    public Double size;

    @Field.PickOne(name = "Permissions", values = {"Shared", "Private"}, required = false,
            description = "Best-effort: whether the file has been shared with anyone beyond its owner.")
    public String permissions;

    @Field.Boolean(name = "Is Folder", required = false)
    public Boolean isFolder;

    @Field.Desc(name = "Tags", type = "[ Text ]", required = false)
    public List<String> tags;

    @Field.Desc(name = "Shared With", type = "[ Text ]", required = false)
    public List<String> sharedWith;

    @Field.Text(name = "Created By", required = false)
    public String createdBy;

    @Field.Text(name = "Modified By", required = false)
    public String modifiedBy;

    @Field.File(name = "File", required = false, description = "Not populated by list operations — use Download as File / Download File using the File Id.")
    public app.krista.model.base.File file;

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
        fields.put("Tags", tags);
        fields.put("Shared With", sharedWith);
        fields.put("Created By", createdBy);
        fields.put("Modified By", modifiedBy);
        fields.put("File", file);
        return fields;
    }

    public static FileEntity fromGraph(DriveItem item, String driveId) {
        FileEntity entity = new FileEntity();
        entity.name = item.name;
        int dot = item.name != null ? item.name.lastIndexOf('.') : -1;
        entity.baseName = dot > 0 ? item.name.substring(0, dot) : item.name;
        entity.extension = dot > 0 ? item.name.substring(dot + 1) : "";
        String absolutePath = SharePointPathResolver.absolutePathOf(item);
        int lastSlash = absolutePath.lastIndexOf('/');
        entity.parentFolder = lastSlash > 0 ? absolutePath.substring(0, lastSlash) : "/";
        entity.absolutePath = absolutePath;
        entity.owner = item.createdBy != null && item.createdBy.user != null ? item.createdBy.user.displayName : null;
        entity.created = item.createdDateTime != null ? item.createdDateTime.toInstant().toEpochMilli() : null;
        entity.modified = item.lastModifiedDateTime != null ? item.lastModifiedDateTime.toInstant().toEpochMilli() : null;
        entity.size = item.size != null ? item.size.doubleValue() : null;
        entity.permissions = item.shared != null ? "Shared" : "Private";
        entity.isFolder = item.folder != null;
        entity.tags = List.of();
        entity.sharedWith = List.of();
        entity.createdBy = entity.owner;
        entity.modifiedBy = item.lastModifiedBy != null && item.lastModifiedBy.user != null
                ? item.lastModifiedBy.user.displayName : null;
        entity.file = null;
        return entity;
    }

    public static String compositeId(DriveItem item, String driveId) {
        return SharePointPathResolver.encodeCompositeId(driveId, item.id);
    }
}
