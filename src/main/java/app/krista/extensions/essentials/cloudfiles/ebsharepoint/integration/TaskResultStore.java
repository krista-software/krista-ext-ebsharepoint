package app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration;

import app.krista.extensions.util.KeyValueStore;
import com.google.gson.Gson;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

/**
 * Buffers the result of a List Files / List Folders / async-download request under a generated Task
 * ID, backed by the platform KeyValueStore — the mechanism behind this extension's reused
 * QUERY_SYSTEM-returns-Task-ID / WAIT_FOR_EVENT-resolves-it request pairs (List Files + Get Files List
 * Result, List Folders + Get Folders List Result, Wait For Event Request... + Get the Downloaded
 * file). The underlying Graph call is made eagerly, before the Task ID is returned — Graph list/content
 * calls are fast HTTP round trips, so there is no real background job to poll; the Task ID indirection
 * exists purely to match the Cloud Files domain's established request shape.
 */
@Service
public class TaskResultStore {

    private static final Gson GSON = new Gson();
    private static final String PREFIX = "ebsp_task_";

    private final KeyValueStore keyValueStore;

    @Inject
    public TaskResultStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    public <T> String save(T payload) {
        String taskId = PREFIX + UUID.randomUUID();
        keyValueStore.put(taskId, GSON.toJson(payload));
        return taskId;
    }

    public <T> T load(String taskId, Type type) {
        Object stored = keyValueStore.get(taskId);
        if (stored == null) {
            return null;
        }
        return GSON.fromJson(String.valueOf(stored), type);
    }

    public String saveDownloadReference(String sourcePath) {
        String taskId = PREFIX + UUID.randomUUID();
        keyValueStore.put(taskId, GSON.toJson(Map.of("sourcePath", sourcePath)));
        return taskId;
    }

    @SuppressWarnings("unchecked")
    public String loadDownloadReference(String taskId) {
        Object stored = keyValueStore.get(taskId);
        if (stored == null) {
            return null;
        }
        Map<String, Object> map = GSON.fromJson(String.valueOf(stored), Map.class);
        Object sourcePath = map.get("sourcePath");
        return sourcePath != null ? sourcePath.toString() : null;
    }

    public void remove(String taskId) {
        keyValueStore.remove(taskId);
    }
}
