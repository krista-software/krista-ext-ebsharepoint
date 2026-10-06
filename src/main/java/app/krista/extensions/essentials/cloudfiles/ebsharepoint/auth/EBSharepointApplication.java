package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

/** Registers EBSharepointAuthResource (Authentication tab) and EBSharepointAuditResource (Audit tab). */
@Service
@ApplicationPath("ebsharepoint")
@ContractsProvided(Application.class)
public class EBSharepointApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(EBSharepointAuthResource.class, EBSharepointAuditResource.class);
    }
}
