package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.SharePointPathResolver;
import com.microsoft.graph.models.DriveItem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic file-or-folder listing item — reused verbatim from the existing Cloud Files domain entity
 * "Custom File" (localDomainEntity_3e0f56dd-...), used by the folder-by-id/path listing requests
 * (Retrieve files using folder id, Get files and File Id from Folder Id, Fetch Files From Folder),
 * which don't distinguish files from folders in their own naming — the "Is Folder" flag does that.
 */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
@Entity(name = "Custom File",
        id = "localDomainEntity_3e0f56dd-ecad-437f-8c8e-5c3d4e3e502c",
        primaryKey = "File Id",
        supportStore = false,
        description = "A file or folder entry returned by a folder listing, reused from the existing Cloud Files domain shape.")
public class CustomFileEntity {

    @ToString
    @Field.Text(name = "Name", required = false)
    public String name;

    @Field.Text(name = "Base Name", required = false, description = "The file name without its extension.")
    public String baseName;

    @Field.Text(name = "Extension", required = false)
    public String extension;

    @Field.Text(name = "File Id", required = false, description = "Opaque identifier (driveId::itemId) usable by Download File using the File Id.")
    public String fileId;

    @Field.Date(name = "Created", required = false)
    public Long created;

    @Field(name = "Size", type = "Number", required = false)
    public Double size;

    @Field.Text(name = "Absolute Path", required = false)
    public String absolutePath;

    @Field.Text(name = "Parent Folder", required = false)
    public String parentFolder;

    @Field.Boolean(name = "Is Folder", required = false)
    public Boolean isFolder;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Name", name);
        fields.put("Base Name", baseName);
        fields.put("Extension", extension);
        fields.put("File Id", fileId);
        fields.put("Created", created);
        fields.put("Size", size);
        fields.put("Absolute Path", absolutePath);
        fields.put("Parent Folder", parentFolder);
        fields.put("Is Folder", isFolder);
        return fields;
    }

    public static CustomFileEntity fromGraph(DriveItem item, String driveId) {
        CustomFileEntity entity = new CustomFileEntity();
        entity.name = item.name;
        int dot = item.name != null ? item.name.lastIndexOf('.') : -1;
        entity.baseName = dot > 0 ? item.name.substring(0, dot) : item.name;
        entity.extension = dot > 0 ? item.name.substring(dot + 1) : "";
        entity.fileId = SharePointPathResolver.encodeCompositeId(driveId, item.id);
        entity.created = item.createdDateTime != null ? item.createdDateTime.toInstant().toEpochMilli() : null;
        entity.size = item.size != null ? item.size.doubleValue() : null;
        String absolutePath = SharePointPathResolver.absolutePathOf(item);
        entity.absolutePath = absolutePath;
        int lastSlash = absolutePath.lastIndexOf('/');
        entity.parentFolder = lastSlash > 0 ? absolutePath.substring(0, lastSlash) : "/";
        entity.isFolder = item.folder != null;
        return entity;
    }
}
