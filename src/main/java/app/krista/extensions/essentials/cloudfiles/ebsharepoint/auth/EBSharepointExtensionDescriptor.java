package app.krista.extensions.essentials.cloudfiles.ebsharepoint.auth;

import app.krista.sdk.msauth.extension.registry.MsAuthExtensionDescriptor;

/** ServiceLoader descriptor that points the microsoft-auth-sdk at EBSharepointAuthConfig. */
public class EBSharepointExtensionDescriptor implements MsAuthExtensionDescriptor {
    @Override
    public Class<?> getConfigClass() {
        return EBSharepointAuthConfig.class;
    }
}
