package fi.metatavu.metaform.test.functional.settings;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utility class for retrieving functional test settings
 *
 * @author Antti Leppä
 */
public class TestSettings {

    private TestSettings() {
        // Zero-argument constructor
    }

    /**
     * Returns API service base path
     */
    public static String getApiBasePath() {
        return "http://localhost:1234/v1";
    }

    /**
     * Returns Keycloak host
     */
    public static String getKeycloakHost() {
        return "http://test-keycloak:8080/auth";
    }

    /**
     * Returns Keycloak realm
     */
    public static String getKeycloakRealm() {
        return "test-1";
    }

    /**
     * Returns Keycloak client id
     */
    public static String getKeycloakClientId() {
        return "metaform-api";
    }

    /**
     * Returns Keycloak client secret
     */
    public static String getKeycloakClientSecret() {
        return "378833f9-dde8-4443-84ca-edfa26e2f0ee";
    }

}
