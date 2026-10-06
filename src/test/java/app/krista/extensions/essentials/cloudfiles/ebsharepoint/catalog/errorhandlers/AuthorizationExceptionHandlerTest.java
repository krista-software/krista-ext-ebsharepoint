package app.krista.extensions.essentials.cloudfiles.ebsharepoint.catalog.errorhandlers;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationExceptionHandlerTest {

    @Test
    void operatorContextConvertsToFailureResponse() throws MustAuthorizeException {
        MustAuthorizeException e = new MustAuthorizeException("not configured", List.of());
        ExtensionResponse response = AuthorizationExceptionHandler.handleAuthorizationException(e, false);
        assertNotNull(response);
    }

    @Test
    void perUserContextRethrows() {
        MustAuthorizeException e = new MustAuthorizeException("sign in required", List.of());
        assertThrows(MustAuthorizeException.class,
                () -> AuthorizationExceptionHandler.handleAuthorizationException(e, true));
    }
}
