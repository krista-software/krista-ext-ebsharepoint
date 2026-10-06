package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.ksdk.context.RequestContext;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities.SiteEntity;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.errorhandlers.AuthorizationExceptionHandler;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.GraphErrorTranslator;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.SharePointGraphService;
import com.microsoft.graph.models.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Operations area — tenant-level SharePoint site discovery. */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
public class OperationsArea {

    private static final Logger LOG = LoggerFactory.getLogger(OperationsArea.class);

    private final SharePointGraphService graphService;
    private final RequestContext requestContext;

    @Inject
    public OperationsArea(SharePointGraphService graphService, RequestContext requestContext) {
        this.graphService = graphService;
        this.requestContext = requestContext;
    }

    /**
     * Reused verbatim from the existing Cloud Files domain request "List User Sites"
     * (localDomainRequest_f32f165f-...) — fulfils the project requirement's "list all sites".
     */
    @CatalogRequest(
            id = "localDomainRequest_f32f165f-8924-47d8-8e50-ca0873f6a5a7",
            name = "List User Sites",
            description = "Retrieves all SharePoint sites the user has access to",
            area = "Operations",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "Sites", type = "[ Entity(Site) ]", required = false)
    @Field.Text(name = "Message", required = false)
    @Field.Boolean(name = "Success", required = false)
    public ExtensionResponse listUserSites(
            @Field.Text(name = "Search", required = false,
                    description = "Filter sites by name. Leave blank to list every site (Microsoft's own \"*\" wildcard is applied automatically).",
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String search) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            List<Site> sites = graphService.listSites(search);
            List<SiteEntity> entities = new ArrayList<>();
            for (Site s : sites) {
                entities.add(SiteEntity.fromGraph(s));
            }
            out.put("Sites", entities);
            out.put("Message", "Found " + entities.size() + " site(s).");
            out.put("Success", true);
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("List User Sites failed", e);
            out.put("Sites", List.of());
            out.put("Message", GraphErrorTranslator.toFriendlyMessage(e));
            out.put("Success", false);
            return new ExtensionResponseBuilder().success(out).build();
        }
    }
}
