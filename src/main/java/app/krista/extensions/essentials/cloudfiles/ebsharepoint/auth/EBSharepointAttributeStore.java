package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.extensions.util.KeyValueStore;
import com.google.gson.Gson;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * Persists EBSharepointMsAuthAttributes as JSON in the platform KeyValueStore.
 *
 * Two uses:
 *  - save(attrs, invokerId) / load(invokerId): the permanent per-invoker config.
 *  - save(attrs) -> temporary "authContextId_<uuid>" key: carries config through a browser OAuth
 *    round-trip during validate-before-save; the caller removes the key in a finally block.
 *
 * KeyValueStore handles encryption at rest — do not add a separate encryption layer here.
 */
@Service
public class EBSharepointAttributeStore {

    private static final Logger LOG = LoggerFactory.getLogger(EBSharepointAttributeStore.class);
    private static final Gson GSON = new Gson();

    private final KeyValueStore keyValueStore;

    @Inject
    public EBSharepointAttributeStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /** Permanent: keyed by invoker id. */
    public void save(EBSharepointMsAuthAttributes attributes, String invokerId) {
        keyValueStore.put(invokerId, GSON.toJson(attributes.toMap()));
        LOG.info("Saved attributes for invoker {}", invokerId);
    }

    /** Transient: random authContextId key (remove in a finally block). */
    public String save(EBSharepointMsAuthAttributes attributes) {
        String key = "authContextId_" + UUID.randomUUID();
        keyValueStore.put(key, GSON.toJson(attributes.toMap()));
        return key;
    }

    @SuppressWarnings("unchecked")
    public EBSharepointMsAuthAttributes load(String key) {
        Object stored = keyValueStore.get(key);
        if (stored == null) {
            return null;
        }
        try {
            Map<String, Object> map = GSON.fromJson(String.valueOf(stored), Map.class);
            return EBSharepointMsAuthAttributes.create(map);
        } catch (Exception e) {
            LOG.error("Failed to deserialize attributes for key {}", key, e);
            return null;
        }
    }

    public void remove(String key) {
        keyValueStore.remove(key);
    }
}
