package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.msauth.token.ITokenStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

/**
 * The ONE persistent token store this extension uses — backed by the platform KeyValueStore, which
 * handles encryption at rest. MUST be injected everywhere an ITokenStore is needed (the Authentication
 * tab's OAuth callback/testConnection AND the Graph client provider used for real business logic) — a
 * locally-constructed InMemoryTokenStore would be invisible to the other side. See Trap 0.11.
 */
@Service
public class EBSharepointTokenStore implements ITokenStore {

    private final KeyValueStore keyValueStore;

    @Inject
    public EBSharepointTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    @Override
    public void save(String key, String value) {
        keyValueStore.put(key, value);
    }

    @Override
    public String get(String key) {
        Object raw = keyValueStore.get(key, Object.class);
        return raw != null ? raw.toString() : null;
    }

    @Override
    public void delete(String key) {
        keyValueStore.remove(key);
    }
}
