package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.errorhandlers;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.ExtensionResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes a MustAuthorizeException from any real catalog request based on
 * RequestContext#invokeAsUser(). This extension is single-mailbox-per-invoker, so the per-user branch
 * is unreachable today, but every request still routes through this handler so the failure mode, if it
 * is ever hit, is a clear exception rather than a silently-wrong result. See
 * adding-krista-msauth-tabs/SKILL.md Trap 0.12.
 */
public final class AuthorizationExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationExceptionHandler.class);

    private AuthorizationExceptionHandler() {
    }

    public static ExtensionResponse handleAuthorizationException(MustAuthorizeException exception, boolean invokeAsUser)
            throws MustAuthorizeException {
        LOG.info("Handling MustAuthorizeException, invokeAsUser={}, message={}", invokeAsUser, exception.getMessage());
        if (invokeAsUser) {
            throw exception;
        }
        return new ExtensionResponseBuilder()
                .failure("Authentication error: " + exception.getMessage())
                .build();
    }
}
