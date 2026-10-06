package app.krista.extensions.essentials.cloudfiles.ebsharepoint.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphErrorTranslatorTest {

    @Test
    void translatesInvalidClientId() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("AADSTS700016: Application with identifier 'x' was not found"));
        assertTrue(msg.contains("Invalid Client ID"));
    }

    @Test
    void translatesInvalidClientSecret() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("AADSTS7000215: Invalid client secret provided"));
        assertTrue(msg.contains("Invalid Client Secret"));
    }

    @Test
    void translatesTenantNotFound() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("AADSTS90002: Tenant 'x' not found"));
        assertTrue(msg.contains("Tenant ID not found"));
    }

    @Test
    void translatesConsentRequired() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("AADSTS65001: consent required"));
        assertTrue(msg.contains("Admin consent"));
    }

    @Test
    void translatesForbidden() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("Authorization_RequestDenied: Insufficient privileges"));
        assertTrue(msg.contains("Access denied"));
    }

    @Test
    void translatesUnauthorized() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("401 Unauthorized"));
        assertTrue(msg.contains("Authentication failed"));
    }

    @Test
    void translatesNotFound() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("itemNotFound: 404"));
        assertTrue(msg.toLowerCase().contains("could not be found"));
    }

    @Test
    void fallsBackToGenericMessage() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException("some completely unrelated failure"));
        assertEquals("Unable to connect to Microsoft SharePoint. Please verify your credentials and try again.", msg);
    }

    @Test
    void handlesNullMessageGracefully() {
        String msg = GraphErrorTranslator.toFriendlyMessage(new RuntimeException((String) null));
        assertNotNull(msg);
    }
}
