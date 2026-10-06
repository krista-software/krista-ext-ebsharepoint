package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.ksdk.context.RequestContext;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities.CustomFileEntity;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities.FileEntity;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.entities.FolderEntity;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.errorhandlers.AuthorizationExceptionHandler;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.GraphErrorTranslator;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.KristaMediaClient;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.SharePointGraphService;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.SharePointPathResolver;
import app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration.TaskResultStore;
import com.google.gson.reflect.TypeToken;
import com.microsoft.graph.models.DriveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Management area — file/folder browsing and download against SharePoint document libraries. Every
 * request here is reused verbatim (same title/input/output shape) from the existing Cloud Files domain
 * catalog, per the approved domain-reuse review, except the three plumbing companions flagged in
 * plan.md (Get Files List Result, Get Folders List Result, Wait For Event Request to get the file
 * using source path) which complete the reused Task-ID-based requests' async shape.
 */
@Domain(id = "catEntryDomain_30c63e77-37f2-4a1d-afd8-f6bfda07ad57",
        name = "Cloud Files",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "f908fbe7-79d9-4f84-a28c-7d5ded1df6b4")
public class ManagementArea {

    private static final Logger LOG = LoggerFactory.getLogger(ManagementArea.class);
    private static final Type FILE_LIST_TYPE = new TypeToken<List<FileEntity>>() { }.getType();
    private static final Type FOLDER_LIST_TYPE = new TypeToken<List<FolderEntity>>() { }.getType();
    private static final Type CUSTOM_FILE_LIST_TYPE = new TypeToken<List<CustomFileEntity>>() { }.getType();

    private final SharePointGraphService graphService;
    private final TaskResultStore taskResultStore;
    private final KristaMediaClient mediaClient;
    private final RequestContext requestContext;

    @Inject
    public ManagementArea(SharePointGraphService graphService, TaskResultStore taskResultStore,
                           KristaMediaClient mediaClient, RequestContext requestContext) {
        this.graphService = graphService;
        this.taskResultStore = taskResultStore;
        this.mediaClient = mediaClient;
        this.requestContext = requestContext;
    }

    // ------------------------------------------------------------------------------------------
    // List Files  (reused: localDomainRequest_f5f0a870-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_f5f0a870-d4ab-4194-b673-1142f21c005a",
            name = "List Files",
            description = "Produces a non-recursive list of files that is present within the folder denoted by the source path",
            area = "Management",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Text(name = "Task ID", required = false)
    public ExtensionResponse listFiles(
            @Field.Text(name = "Source Path", required = true,
                    description = "A full path: /Site Name/Document Library/optional/sub/folder",
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String sourcePath,
            @Field.Text(name = "Search", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String search) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SharePointGraphService.DriveContext ctx = graphService.resolvePathToDrive(sourcePath);
            List<DriveItem> children = graphService.listChildrenAtPath(ctx.driveId(), ctx.remainderPath());
            List<FileEntity> files = new ArrayList<>();
            for (DriveItem item : children) {
                if (item.folder == null && matchesSearch(item.name, search)) {
                    files.add(FileEntity.fromGraph(item, ctx.driveId()));
                }
            }
            out.put("Task ID", taskResultStore.save(files));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("List Files failed for path {}", sourcePath, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Get Files List Result  (new — companion completing List Files' Task ID)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_eb5p0000-aaaa-4aaa-8aaa-000000000001",
            name = "Get Files List Result",
            description = "Resolves the file list produced by List Files for the given Task ID",
            area = "Management",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Files", type = "[ Entity(File) ]", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse getFilesListResult(
            @Field.Text(name = "Task ID", required = true, attributes = {}, options = {}) String taskId) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<FileEntity> files = taskResultStore.load(taskId, FILE_LIST_TYPE);
        if (files == null) {
            out.put("Files", List.of());
            out.put("Success", false);
            out.put("Message", "No result found for Task ID \"" + taskId + "\" — it may have already been retrieved or expired.");
            return new ExtensionResponseBuilder().success(out).build();
        }
        taskResultStore.remove(taskId);
        out.put("Files", files);
        out.put("Success", true);
        out.put("Message", "Found " + files.size() + " file(s).");
        return new ExtensionResponseBuilder().success(out).build();
    }

    // ------------------------------------------------------------------------------------------
    // List Folders  (reused: localDomainRequest_3f16a853-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_3f16a853-5d28-4af7-8d4c-6f8eab2d341a",
            name = "List Folders",
            description = "Produces a non-recursive list of folders that is present within the folder denoted by the source path",
            area = "Management",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Text(name = "Task ID", required = false)
    public ExtensionResponse listFolders(
            @Field.Text(name = "Source Path", required = true,
                    description = "A full path: /Site Name/Document Library/optional/sub/folder",
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String sourcePath,
            @Field.Text(name = "Search", required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {}) String search) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SharePointGraphService.DriveContext ctx = graphService.resolvePathToDrive(sourcePath);
            List<DriveItem> children = graphService.listChildrenAtPath(ctx.driveId(), ctx.remainderPath());
            List<FolderEntity> folders = new ArrayList<>();
            for (DriveItem item : children) {
                if (item.folder != null && matchesSearch(item.name, search)) {
                    folders.add(FolderEntity.fromGraph(item, ctx.driveId()));
                }
            }
            out.put("Task ID", taskResultStore.save(folders));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("List Folders failed for path {}", sourcePath, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Get Folders List Result  (new — companion completing List Folders' Task ID)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_eb5p0000-aaaa-4aaa-8aaa-000000000002",
            name = "Get Folders List Result",
            description = "Resolves the folder list produced by List Folders for the given Task ID",
            area = "Management",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Folders", type = "[ Entity(Folder) ]", required = false)
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse getFoldersListResult(
            @Field.Text(name = "Task ID", required = true, attributes = {}, options = {}) String taskId) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<FolderEntity> folders = taskResultStore.load(taskId, FOLDER_LIST_TYPE);
        if (folders == null) {
            out.put("Folders", List.of());
            out.put("Success", false);
            out.put("Message", "No result found for Task ID \"" + taskId + "\" — it may have already been retrieved or expired.");
            return new ExtensionResponseBuilder().success(out).build();
        }
        taskResultStore.remove(taskId);
        out.put("Folders", folders);
        out.put("Success", true);
        out.put("Message", "Found " + folders.size() + " folder(s).");
        return new ExtensionResponseBuilder().success(out).build();
    }

    // ------------------------------------------------------------------------------------------
    // Download as File  (reused: localDomainRequest_d0aea3ac-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_d0aea3ac-2059-4ce4-85e8-626b42e64c0c",
            name = "Download as File",
            description = "Retrieves the File from the cloud storage and provides a reference as Krista File based on provided source location.",
            area = "Management",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.File(name = "Krista File", required = false, attributes = {}, options = {})
    @Field.Boolean(name = "Success", required = false)
    @Field(name = "Message", type = "RichText", required = false)
    public ExtensionResponse downloadAsFile(
            @Field.Text(name = "Source", required = true,
                    description = "A full path: /Site Name/Document Library/folder/file.ext",
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String source) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SharePointGraphService.DriveContext ctx = graphService.resolvePathToDrive(source);
            SharePointGraphService.DownloadedContent content = graphService.downloadAtPath(ctx.driveId(), ctx.remainderPath());
            File tempFile = mediaClient.bytesToTempFile(content.bytes(), content.fileName());
            app.krista.model.base.File kristaFile = mediaClient.toKristaFile(tempFile);
            out.put("Krista File", kristaFile);
            out.put("Success", true);
            out.put("Message", "Downloaded " + content.fileName() + ".");
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("Download as File failed for source {}", source, e);
            out.put("Krista File", null);
            out.put("Success", false);
            out.put("Message", GraphErrorTranslator.toFriendlyMessage(e));
            return new ExtensionResponseBuilder().success(out).build();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Download File using the File Id  (reused: localDomainRequest_4e95bf64-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_4e95bf64-d4b8-40e9-9ae6-836b9a71a48b",
            name = "Download File using the File Id",
            description = "Downloads a file identified by the opaque File Id returned by List Files / Get Files List Result.",
            area = "Management",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.File(name = "File", required = false, attributes = {}, options = {})
    public app.krista.model.base.File downloadFileUsingTheFileId(
            @Field.Text(name = "File Id", required = true, attributes = {}, options = {}) String fileId) {
        try {
            SharePointPathResolver.CompositeId id = SharePointPathResolver.decodeCompositeId(fileId);
            SharePointGraphService.DownloadedContent content = graphService.downloadById(id.driveId(), id.itemId());
            File tempFile = mediaClient.bytesToTempFile(content.bytes(), content.fileName());
            return mediaClient.toKristaFile(tempFile);
        } catch (MustAuthorizeException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Download File using the File Id failed for id {}", fileId, e);
            throw new RuntimeException(GraphErrorTranslator.toFriendlyMessage(e), e);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Wait For Event Request to get the file using source path  (new — companion producing a Task
    // Id for the async download pair below, completing the already-approved reuse of
    // "Get the Downloaded file")
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_eb5p0000-aaaa-4aaa-8aaa-000000000003",
            name = "Wait For Event Request to get the file using source path",
            description = "Schedules a file download by source path and returns a Task Id, resolved by Get the Downloaded file.",
            area = "Management",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Text(name = "Task Id", required = false)
    public ExtensionResponse waitForEventRequestToGetTheFileUsingSourcePath(
            @Field.Text(name = "Source Path", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String sourcePath) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SharePointGraphService.DriveContext ctx = graphService.resolvePathToDrive(sourcePath);
            SharePointGraphService.DownloadedContent content = graphService.downloadAtPath(ctx.driveId(), ctx.remainderPath());
            out.put("Task Id", taskResultStore.save(content));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("Wait For Event Request to get the file using source path failed for {}", sourcePath, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Get the Downloaded file  (reused: localDomainRequest_a151c209-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_a151c209-1326-43fe-aa78-acca7a0bbfa0",
            name = "Get the Downloaded file",
            description = "Get the downloaded file",
            area = "Management",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.File(name = "File", required = false, attributes = {}, options = {})
    @Field.Boolean(name = "Success", required = false)
    @Field.Text(name = "Message", required = false)
    public ExtensionResponse getTheDownloadedFile(
            @Field.Text(name = "Task Id", required = true, attributes = {}, options = {}) String taskId) {
        Map<String, Object> out = new LinkedHashMap<>();
        SharePointGraphService.DownloadedContent content = taskResultStore.load(taskId, SharePointGraphService.DownloadedContent.class);
        if (content == null) {
            out.put("File", null);
            out.put("Success", false);
            out.put("Message", "No result found for Task Id \"" + taskId + "\" — it may have already been retrieved or expired.");
            return new ExtensionResponseBuilder().success(out).build();
        }
        try {
            taskResultStore.remove(taskId);
            File tempFile = mediaClient.bytesToTempFile(content.bytes(), content.fileName());
            out.put("File", mediaClient.toKristaFile(tempFile));
            out.put("Success", true);
            out.put("Message", "Downloaded " + content.fileName() + ".");
        } catch (Exception e) {
            LOG.error("Get the Downloaded file failed for task {}", taskId, e);
            out.put("File", null);
            out.put("Success", false);
            out.put("Message", GraphErrorTranslator.toFriendlyMessage(e));
        }
        return new ExtensionResponseBuilder().success(out).build();
    }

    // ------------------------------------------------------------------------------------------
    // Retrieve files using folder id  (reused: localDomainRequest_fcefb078-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_fcefb078-1ed1-4df5-8a48-cc82b17e39d9",
            name = "Retrieve files using folder id",
            description = "Request Description",
            area = "Management",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Response", type = "[ Entity(Custom File) ]", required = false)
    public ExtensionResponse retrieveFilesUsingFolderId(
            @Field.Text(name = "Folder Id", required = true, attributes = {}, options = {}) String folderId,
            @Field.Date(name = "From Date", required = false, attributes = {}, options = {}) Long fromDate) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("Response", listCustomFilesByFolderId(folderId, fromDate));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("Retrieve files using folder id failed for {}", folderId, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Get files and File Id from Folder Id  (reused: localDomainRequest_a5e85205-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_a5e85205-cc78-4107-ab93-e7468d6c0bdc",
            name = "Get files and File Id from Folder Id",
            description = "Request Description",
            area = "Management",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "List of Files", type = "[ Entity(Custom File) ]", required = false)
    public ExtensionResponse getFilesAndFileIdFromFolderId(
            @Field.Text(name = "Folder Id", required = true, attributes = {}, options = {}) String folderId,
            @Field.Date(name = "From Date", required = false, attributes = {}, options = {}) Long fromDate) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            out.put("List of Files", listCustomFilesByFolderId(folderId, fromDate));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("Get files and File Id from Folder Id failed for {}", folderId, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    private List<CustomFileEntity> listCustomFilesByFolderId(String folderId, Long fromDate) throws java.io.IOException {
        SharePointPathResolver.CompositeId id = SharePointPathResolver.decodeCompositeId(folderId);
        List<DriveItem> children = graphService.listChildrenById(id.driveId(), id.itemId());
        List<CustomFileEntity> results = new ArrayList<>();
        for (DriveItem item : children) {
            if (matchesFromDate(item, fromDate)) {
                results.add(CustomFileEntity.fromGraph(item, id.driveId()));
            }
        }
        return results;
    }

    // ------------------------------------------------------------------------------------------
    // Fetch Files From Folder  (reused: localDomainRequest_7f15df55-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_7f15df55-3300-4dda-8a0d-5ce6d1f64712",
            name = "Fetch Files From Folder",
            description = "Request Description",
            area = "Management",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Response", type = "[ Entity(Custom File) ]", required = false)
    public ExtensionResponse fetchFilesFromFolder(
            @Field.Text(name = "Path", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String path,
            @Field.Date(name = "From Date", required = false, attributes = {}, options = {}) Long fromDate) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SharePointGraphService.DriveContext ctx = graphService.resolvePathToDrive(path);
            List<DriveItem> children = graphService.listChildrenAtPath(ctx.driveId(), ctx.remainderPath());
            List<CustomFileEntity> results = new ArrayList<>();
            for (DriveItem item : children) {
                if (matchesFromDate(item, fromDate)) {
                    results.add(CustomFileEntity.fromGraph(item, ctx.driveId()));
                }
            }
            out.put("Response", results);
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("Fetch Files From Folder failed for {}", path, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Get Folder Id by path  (reused: localDomainRequest_3513c211-...)
    // ------------------------------------------------------------------------------------------
    @CatalogRequest(
            id = "localDomainRequest_3513c211-3ee6-4ae5-84c3-91a3d6e58dd9",
            name = "Get Folder Id by path",
            description = "Request Description",
            area = "Management",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Text(name = "Folder Id", required = false)
    public ExtensionResponse getFolderIdByPath(
            @Field.Text(name = "Path", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {}) String path) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            SharePointGraphService.DriveContext ctx = graphService.resolvePathToDrive(path);
            DriveItem item = graphService.resolveItemAtPath(ctx.driveId(), ctx.remainderPath());
            if (item.folder == null) {
                return new ExtensionResponseBuilder().failure("\"" + path + "\" is not a folder.").build();
            }
            out.put("Folder Id", SharePointPathResolver.encodeCompositeId(ctx.driveId(), item.id));
            return new ExtensionResponseBuilder().success(out).build();
        } catch (MustAuthorizeException e) {
            return AuthorizationExceptionHandler.handleAuthorizationException(e, requestContext.invokeAsUser());
        } catch (Exception e) {
            LOG.error("Get Folder Id by path failed for {}", path, e);
            return new ExtensionResponseBuilder().failure(GraphErrorTranslator.toFriendlyMessage(e)).build();
        }
    }

    private static boolean matchesSearch(String name, String search) {
        return search == null || search.isBlank() || (name != null && name.toLowerCase().contains(search.toLowerCase()));
    }

    private static boolean matchesFromDate(DriveItem item, Long fromDate) {
        if (fromDate == null) {
            return true;
        }
        if (item.lastModifiedDateTime == null) {
            return false;
        }
        return item.lastModifiedDateTime.toInstant().toEpochMilli() >= fromDate;
    }
}
