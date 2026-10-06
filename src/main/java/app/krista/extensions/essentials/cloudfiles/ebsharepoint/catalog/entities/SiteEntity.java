package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Entity;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.ToString;
import com.microsoft.graph.models.Site;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A SharePoint site. New entity — Cloud Files had no existing site-level entity to reuse (see
 * implementation-plan.json's domainReuse notes).
 */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
@Entity(name = "Site",
        id = "localDomainEntity_eb5p0000-1111-4a11-9a11-ebsp00000001",
        primaryKey = "Web Url",
        supportStore = false,
        description = "A Microsoft SharePoint site.")
public class SiteEntity {

    @Field.Text(name = "Name", required = false, description = "The site's internal (URL-safe) name.")
    public String name;

    @ToString
    @Field.Text(name = "Display Name", required = false, description = "The site's human-readable display name.")
    public String displayName;

    @Field.Text(name = "Web Url", required = false, description = "The site's browser URL.")
    public String webUrl;

    @Field(name = "Description", type = "RichText", required = false, description = "The site's description, if set.")
    public String description;

    @Field.Date(name = "Created", required = false, description = "When the site was created.")
    public Long created;

    @Field.Date(name = "Modified", required = false, description = "When the site was last modified.")
    public Long modified;

    public Map<String, Object> toFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Name", name);
        fields.put("Display Name", displayName);
        fields.put("Web Url", webUrl);
        fields.put("Description", description);
        fields.put("Created", created);
        fields.put("Modified", modified);
        return fields;
    }

    public static SiteEntity fromGraph(Site site) {
        SiteEntity entity = new SiteEntity();
        entity.name = site.name;
        entity.displayName = site.displayName;
        entity.webUrl = site.webUrl;
        entity.description = site.description;
        entity.created = epochMillis(site.createdDateTime);
        entity.modified = epochMillis(site.lastModifiedDateTime);
        return entity;
    }

    static Long epochMillis(OffsetDateTime time) {
        return time != null ? time.toInstant().toEpochMilli() : null;
    }
}
