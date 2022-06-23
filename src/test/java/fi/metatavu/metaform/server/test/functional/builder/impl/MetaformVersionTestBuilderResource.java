package fi.metatavu.metaform.server.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.VersionsApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.MetaformVersion;
import fi.metatavu.metaform.server.test.TestSettings;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test builder resource for metaform versions API
 *
 * @author Tianxing Wu
 */
public class MetaformVersionTestBuilderResource extends ApiTestBuilderResource<MetaformVersion, VersionsApi> {

    private final AccessTokenProvider accessTokenProvider;

    private final Map<UUID, UUID> versionsMetaforms = new HashMap<>();

    public MetaformVersionTestBuilderResource(
        AbstractTestBuilder<ApiClient> testBuilder,
        AccessTokenProvider accessTokenProvider,
        ApiClient apiClient) {
        super(testBuilder, apiClient);
        this.accessTokenProvider = accessTokenProvider;
    }

        @Override
        public VersionsApi getApi() {
        try {
            ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new VersionsApi(TestSettings.basePath);
    }

    @Override
    public void clean(MetaformVersion metaformVersion) throws IOException {
        UUID metaformId = versionsMetaforms.get(metaformVersion.getId());
        getApi().deleteMetaformVersion(metaformId, metaformVersion.getId());
    }

    /**
     * Creates new metaform version
     *
     * @param metaformId metaform id
     * @param payload    payload
     * @return created metaform version
     */
    public MetaformVersion create(UUID metaformId, MetaformVersion payload) throws IOException {
        MetaformVersion result = getApi().createMetaformVersion(metaformId, payload);
        versionsMetaforms.put(result.getId(), metaformId);

        return addClosable(result);
    }

    /**
     * Finds a metaform version
     *
     * @param metaformId metaform id
     * @param versionId  version id
     * @return found metaform version
     */
    public MetaformVersion findVersion(UUID metaformId, UUID versionId) throws IOException {
        return getApi().findMetaformVersion(metaformId, versionId);
    }

    /**
     * Deletes a metaform version from the API
     *
     * @param metaformId      metaform id
     * @param metaformVersion metaform version
     */
    public void delete(UUID metaformId, MetaformVersion metaformVersion) throws IOException {
        Assert.assertNotNull(metaformVersion.getId());
        getApi().deleteMetaformVersion(metaformId, metaformVersion.getId());
        removeCloseable(closable -> {
            if (closable instanceof MetaformVersion) {
                return metaformVersion.getId().equals(((MetaformVersion) closable).getId());
            }
            return false;
        });
    }

    /**
     * Asserts metaform version count within the system
     *
     * @param metaformId metaform id
     * @param expected   expected count
     */
    public void assertCount(UUID metaformId, int expected) throws IOException {
        Assert.assertEquals(expected, getApi().listMetaformVersions(metaformId).length);
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param versionId      version id
     */
    public void assertFindFailStatus(int expectedStatus, UUID metaformId, UUID versionId) throws IOException {
        try {
            getApi().findMetaformVersion(metaformId, versionId);
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus));
        } catch (ClientException e) {
            Assert.assertEquals(expectedStatus, e.getStatusCode());
        }
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param payload        payload
     */
    public void assertCreateFailStatus(int expectedStatus, UUID metaformId, MetaformVersion payload) throws IOException {
        try {
            getApi().createMetaformVersion(metaformId, payload);
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus));
        } catch (ClientException e) {
            Assert.assertEquals(expectedStatus, e.getStatusCode());
        }
    }


    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform
     * @param versionId      version id
     */
    public void assertDeleteFailStatus(int expectedStatus, UUID metaformId, UUID versionId) throws IOException {
        try {
            getApi().deleteMetaformVersion(metaformId, versionId);
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus));
        } catch (ClientException e) {
            Assert.assertEquals(expectedStatus, e.getStatusCode());
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     */
    public void assertListFailStatus(int expectedStatus, UUID metaformId) throws IOException {
        try {
            getApi().listMetaformVersions(metaformId);
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus));
        } catch (ClientException e) {
            Assert.assertEquals(expectedStatus, e.getStatusCode());
        }
    }

    /**
     * Gets example version data
     *
     * @return example version data
     */
    public Map<String, String> getExampleVersionData() {
        Map<String, String> versionData = new HashMap<>();
        versionData.put("formData", "form value");

        return versionData;
    }
}