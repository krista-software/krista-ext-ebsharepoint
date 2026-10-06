# Supported requests

| Area | Request | Type | Summary |
|---|---|---|---|
| Setup | [Test Connection](pages/TestConnection.md) | Query | Validate connectivity and authentication. |
| Setup | [Health Check](pages/HealthCheck.md) | Change | Report connectivity and JVM health. |
| Operations | [List User Sites](pages/ListUserSites.md) | Query | List every SharePoint site the connection can see. |
| Management | [List Files](pages/ListFiles.md) | Query | List files in a folder (returns a Task ID). |
| Management | [Get Files List Result](pages/GetFilesListResult.md) | Wait For Event | Resolve List Files' Task ID. |
| Management | [List Folders](pages/ListFolders.md) | Query | List folders in a folder (returns a Task ID). |
| Management | [Get Folders List Result](pages/GetFoldersListResult.md) | Wait For Event | Resolve List Folders' Task ID. |
| Management | [Download as File](pages/DownloadAsFile.md) | Change | Download a file by path. |
| Management | [Download File using the File Id](pages/DownloadFileUsingTheFileId.md) | Change | Download a file by opaque File Id. |
| Management | [Wait For Event Request to get the file using source path](pages/WaitForEventRequestToGetTheFileUsingSourcePath.md) | Query | Schedule an async download by path (returns a Task Id). |
| Management | [Get the Downloaded file](pages/GetTheDownloadedFile.md) | Wait For Event | Resolve an async download's Task Id. |
| Management | [Retrieve files using folder id](pages/RetrieveFilesUsingFolderId.md) | Change | List a folder's contents by Folder Id. |
| Management | [Get files and File Id from Folder Id](pages/GetFilesAndFileIdFromFolderId.md) | Change | List a folder's contents by Folder Id. |
| Management | [Fetch Files From Folder](pages/FetchFilesFromFolder.md) | Change | List a folder's contents by path. |
| Management | [Get Folder Id by path](pages/GetFolderIdByPath.md) | Change | Resolve a path to an opaque Folder Id. |
