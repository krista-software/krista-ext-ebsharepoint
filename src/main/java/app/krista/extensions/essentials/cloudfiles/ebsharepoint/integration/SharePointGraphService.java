package app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration;

import app.krista.ksdk.context.RequestContext;
import app.krista.extension.executor.Invoker;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.DriveCollectionPage;
import com.microsoft.graph.requests.DriveCollectionRequestBuilder;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemCollectionRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SiteCollectionPage;
import com.microsoft.graph.requests.SiteCollectionRequestBuilder;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * All Microsoft Graph I/O for the SharePoint Sites/Drives/DriveItems resources lives here — isolating
 * the real API-shape risk to one class, and giving every Area a simple, mockable collaborator.
 *
 * Pagination guard: every collection read follows getNextPage() up to MAX_PAGES — a safety bound, not
 * a business limit, against a runaway tenant never terminating a listing.
 */
@Service
public class SharePointGraphService {

    private static final int MAX_PAGES = 25;

    private final EBSharepointGraphClientProvider graphClientProvider;
    private final RequestContext requestContext;
    private final Invoker invoker;

    @Inject
    public SharePointGraphService(EBSharepointGraphClientProvider graphClientProvider,
                                   RequestContext requestContext, Invoker invoker) {
        this.graphClientProvider = graphClientProvider;
        this.requestContext = requestContext;
        this.invoker = invoker;
    }

    private GraphServiceClient<okhttp3.Request> client() throws IOException {
        return graphClientProvider.forRequest(requestContext, invoker.getInvokerId());
    }

    /**
     * A cheap, bounded connectivity probe for Test Connection / Health Check — proves the token and
     * the Sites.Read.All scope actually work, without reading the whole tenant's site collection.
     * Bounded with .top(1), matching the SAME resource family (sites) every real business-logic
     * request in this extension already calls — never `.me()` (Trap 0.14).
     */
    public void probeConnectivity() throws IOException {
        client().sites().buildRequest().top(1).get();
    }

    /**
     * Lists every SharePoint site visible to this connection. Microsoft Graph's /sites resource is a
     * SEARCH endpoint, not a tenant-enumeration one — a blank search returns an empty collection, not
     * every site (HTTP 200, no error). Default to the documented "*" wildcard. See Trap 0.18.
     */
    public List<Site> listSites(String search) throws IOException {
        String effectiveSearch = (search == null || search.isBlank()) ? "*" : search;
        List<Site> all = new ArrayList<>();
        SiteCollectionPage page = client().sites()
                .buildRequest(List.of(new QueryOption("search", effectiveSearch)))
                .get();
        int guard = 0;
        while (page != null && guard++ < MAX_PAGES) {
            all.addAll(page.getCurrentPage());
            SiteCollectionRequestBuilder next = page.getNextPage();
            page = next != null ? next.buildRequest().get() : null;
        }
        return all;
    }

    public Site resolveSiteByName(String siteName) throws IOException {
        List<Site> sites = listSites(siteName);
        for (Site s : sites) {
            if (siteName.equalsIgnoreCase(s.displayName) || siteName.equalsIgnoreCase(s.name)) {
                return s;
            }
        }
        if (!sites.isEmpty()) {
            return sites.get(0);
        }
        throw new NoSuchElementException("No SharePoint site found matching \"" + siteName + "\".");
    }

    public Drive resolveDrive(String siteId, String libraryName) throws IOException {
        if (libraryName == null || libraryName.isBlank()) {
            return client().sites(siteId).drive().buildRequest().get();
        }
        List<Drive> all = new ArrayList<>();
        DriveCollectionPage page = client().sites(siteId).drives().buildRequest().get();
        int guard = 0;
        while (page != null && guard++ < MAX_PAGES) {
            all.addAll(page.getCurrentPage());
            DriveCollectionRequestBuilder next = page.getNextPage();
            page = next != null ? next.buildRequest().get() : null;
        }
        for (Drive d : all) {
            if (libraryName.equalsIgnoreCase(d.name)) {
                return d;
            }
        }
        if (!all.isEmpty()) {
            return all.get(0);
        }
        throw new NoSuchElementException("No document library found matching \"" + libraryName + "\".");
    }

    public record DriveContext(String siteId, String driveId, String remainderPath) {
    }

    /** Resolves this extension's "/Site Name/Document Library/sub/path" convention to a Graph drive. */
    public DriveContext resolvePathToDrive(String rawPath) throws IOException {
        SharePointPathResolver.ParsedPath parsed = SharePointPathResolver.parse(rawPath);
        Site site = resolveSiteByName(parsed.siteName());
        Drive drive = resolveDrive(site.id, parsed.libraryName());
        return new DriveContext(site.id, drive.id, parsed.itemPath());
    }

    public DriveItem resolveItemAtPath(String driveId, String itemPath) throws IOException {
        if (itemPath == null || itemPath.isBlank()) {
            return client().drives(driveId).root().buildRequest().get();
        }
        return client().drives(driveId).root().itemWithPath(itemPath).buildRequest().get();
    }

    public List<DriveItem> listChildrenAtPath(String driveId, String itemPath) throws IOException {
        DriveItemCollectionPage page = (itemPath == null || itemPath.isBlank())
                ? client().drives(driveId).root().children().buildRequest().get()
                : client().drives(driveId).root().itemWithPath(itemPath).children().buildRequest().get();
        return drainDriveItems(page);
    }

    public List<DriveItem> listChildrenById(String driveId, String itemId) throws IOException {
        DriveItemCollectionPage page = client().drives(driveId).items(itemId).children().buildRequest().get();
        return drainDriveItems(page);
    }

    public DriveItem getItemById(String driveId, String itemId) throws IOException {
        return client().drives(driveId).items(itemId).buildRequest().get();
    }

    private List<DriveItem> drainDriveItems(DriveItemCollectionPage page) throws IOException {
        List<DriveItem> all = new ArrayList<>();
        int guard = 0;
        while (page != null && guard++ < MAX_PAGES) {
            all.addAll(page.getCurrentPage());
            DriveItemCollectionRequestBuilder next = page.getNextPage();
            page = next != null ? next.buildRequest().get() : null;
        }
        return all;
    }

    public record DownloadedContent(byte[] bytes, String fileName, String mimeType) {
    }

    public DownloadedContent downloadAtPath(String driveId, String itemPath) throws IOException {
        DriveItem item = resolveItemAtPath(driveId, itemPath);
        if (item.folder != null) {
            throw new IllegalArgumentException("\"" + itemPath + "\" is a folder, not a file — only files can be downloaded.");
        }
        try (InputStream is = (itemPath == null || itemPath.isBlank())
                ? client().drives(driveId).root().content().buildRequest().get()
                : client().drives(driveId).root().itemWithPath(itemPath).content().buildRequest().get()) {
            return new DownloadedContent(is.readAllBytes(), item.name,
                    item.file != null ? item.file.mimeType : "application/octet-stream");
        }
    }

    public DownloadedContent downloadById(String driveId, String itemId) throws IOException {
        DriveItem item = getItemById(driveId, itemId);
        if (item.folder != null) {
            throw new IllegalArgumentException("That identifier points to a folder, not a file — only files can be downloaded.");
        }
        try (InputStream is = client().drives(driveId).items(itemId).content().buildRequest().get()) {
            return new DownloadedContent(is.readAllBytes(), item.name,
                    item.file != null ? item.file.mimeType : "application/octet-stream");
        }
    }
}
