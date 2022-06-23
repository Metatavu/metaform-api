package fi.metatavu.metaform.server.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.RepliesApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.server.test.TestSettings;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test builder resource for replies
 *
 * @author Antti Leppä
 */
public class ReplyTestBuilderResource extends ApiTestBuilderResource<Reply, RepliesApi> {

  private final AccessTokenProvider accessTokenProvider;
  private final Map<UUID, UUID> replyMetaformIds = new HashMap<>();

  public ReplyTestBuilderResource(
    AbstractTestBuilder<ApiClient> testBuilder,
    AccessTokenProvider accessTokenProvider,
    ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  public void clean(Reply reply) throws IOException {
    UUID metaformId = replyMetaformIds.get(reply.getId());
    getApi().deleteReply(metaformId, reply.getId(), null);
  }

  @Override
  protected RepliesApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new RepliesApi(TestSettings.basePath);
  }

  /**
   * Creates new reply
   *
   * @param metaformId     metaform id
   * @param updateExisting whether to update existing reply
   * @param replyMode      reply mode
   * @param payload        payload
   * @return created reply
   */
  public Reply create(UUID metaformId, Boolean updateExisting, String replyMode, Reply payload) throws IOException {
    Reply result = getApi().createReply(metaformId, payload, updateExisting, replyMode);
    replyMetaformIds.put(result.getId(), metaformId);
    return addClosable(result);
  }

  /**
   * Creates new reply
   *
   * @param metaformId metaform id
   * @param replyMode  reply mode
   * @param payload    payload
   * @return created reply
   */
  public Reply create(UUID metaformId, String replyMode, Reply payload) throws IOException {
    return create(metaformId, null, replyMode, payload);
  }

  /**
   * Lists all replies by filters
   *
   * @param metaformId       metaform id
   * @param userId           user id
   * @param createdBefore    createdBefore
   * @param createdAfter     createdAfter
   * @param modifiedBefore   modifiedBefore
   * @param modifiedAfter    modifiedAfter
   * @param includeRevisions includeRevisions
   * @param fields           fields
   * @param firstResult      firstResult
   * @param maxResults       maxResults
   * @return array of found replies
   */
  public Reply[] listReplies(UUID metaformId, UUID userId, String createdBefore, String createdAfter, String modifiedBefore, String modifiedAfter,
                             Boolean includeRevisions, String[] fields, Integer firstResult, Integer maxResults) throws IOException {
    return getApi().listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults);
  }

  /**
   * Lists all replies by metaform
   *
   * @param metaformId metaform id
   * @return all found replies
   */
  public Reply[] listReplies(UUID metaformId) throws IOException {
    return getApi().listReplies(metaformId, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Creates new reply for TBNC form
   *
   * @param metaform  metaform
   * @param text      text value
   * @param bool      boolean value
   * @param number    number value
   * @param checklist checklist value
   * @return reply
   */
  public Reply createTBNCReply(Metaform metaform, String text, Boolean bool, double number, String[] checklist) throws IOException {
    Map<String, Object> replyData = new HashMap<>();
    replyData.put("text", text);
    replyData.put("boolean", bool);
    replyData.put("number", number);
    replyData.put("checklist", checklist);
    Reply reply = createReplyWithData(replyData);
    return getApi().createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
  }

  /**
   * Creates new reply for the simple form
   *
   * @param metaform  metaform
   * @param value     value
   * @param replyMode reply model
   * @return reply
   */
  public Reply createSimpleReply(Metaform metaform, String value, ReplyMode replyMode) throws IOException {
    Map<String, Object> replyData1 = new HashMap<>();
    replyData1.put("text", value);
    Reply reply = createReplyWithData(replyData1);
    return create(metaform.getId(), replyMode.toString(), reply);
  }

  /**
   * Asserts reply can not be found with given owner key
   *
   * @param metaform metaform
   * @param reply    reply
   * @param ownerKey owner key
   */
  public void assertReplyOwnerKeyFindForbidden(Metaform metaform, Reply reply, String ownerKey) throws IOException {
    try {
      getApi().findReply(metaform.getId(), reply.getId(), ownerKey);
      fail(String.format("Should not be able to find reply %s", reply.getId().toString()));
    } catch (ClientException e) {
      assertEquals(403, e.getStatusCode());
    }
  }

  /**
   * Finds a reply
   *
   * @param metaformId metaform id
   * @param replyId    reply id
   * @param ownerKey   owner key
   * @return found reply
   */
  public Reply findReply(UUID metaformId, UUID replyId, String ownerKey) throws IOException {
    return getApi().findReply(metaformId, replyId, ownerKey);
  }

  /**
   * Updates a reply into the API
   *
   * @param metaformId metaform id
   * @param body       body payload
   * @param ownerKey   owner key
   */
  public void updateReply(UUID metaformId, UUID replyId, Reply body, String ownerKey) throws IOException {
    getApi().updateReply(metaformId, replyId, body, ownerKey);
  }

  /**
   * Deletes a reply from the API
   *
   * @param metaformId metaform id
   * @param reply      reply to be deleted
   * @param ownerKey   owner key
   */
  public void delete(UUID metaformId, Reply reply, String ownerKey) throws IOException {
    assertNotNull(reply.getId());
    getApi().deleteReply(metaformId, reply.getId(), ownerKey);

    removeCloseable(closable -> {
      if (closable instanceof Reply) {
        return reply.getId().equals(((Reply) closable).getId());
      }

      return false;
    });
  }

  /**
   * Asserts reply count within the system
   *
   * @param expected         expected count
   * @param metaformId       Metaform id (required)
   * @param userId           Filter results by user id. If this parameter is not specified all replies are returned, this requires logged user to have proper permission to do so (optional)
   * @param createdBefore    Filter results created before specified time (optional)
   * @param createdAfter     Filter results created after specified time (optional)
   * @param modifiedBefore   Filter results modified before specified time (optional)
   * @param modifiedAfter    Filter results modified after specified time (optional)
   * @param includeRevisions Specifies that revisions should be included into response (optional)
   * @param fields           Filter results by field values. Format is field:value, multiple values can be added by using comma separator. E.g. field1&#x3D;value,field2&#x3D;another (optional)
   * @param firstResult      First index of results to be returned (optional)
   * @param maxResults       How many items to return at one time (optional)
   */
  public void assertCount(int expected, UUID metaformId, UUID userId, String createdBefore, String createdAfter, String modifiedBefore, String modifiedAfter, Boolean includeRevisions, String[] fields, Integer firstResult, Integer maxResults) throws IOException {
    assertEquals(expected, getApi().listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults).length);
  }

  /**
   * Asserts create simple reply status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaform       metaform id
   * @param value          value
   * @param replyMode      replyMode
   */
  public void assertCreateSimpleReplyFail(int expectedStatus, Metaform metaform, String value, ReplyMode replyMode) throws IOException {
    try {
      createSimpleReply(metaform, value, replyMode);
      fail(String.format("Expected find to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }


  /**
   * Asserts find status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   * @param replyId        reply id
   * @param ownerKey       owner key
   */
  public void assertFindFailStatus(int expectedStatus, UUID metaformId, UUID replyId, String ownerKey) throws IOException {
    try {
      getApi().findReply(metaformId, replyId, ownerKey);
      fail(String.format("Expected find to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts create status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   * @param updateExisting whether to update existing reply
   * @param replyMode      reply mode
   * @param payload        payload
   */
  public void assertCreateFailStatus(int expectedStatus, UUID metaformId, Boolean updateExisting, String replyMode, Reply payload) throws IOException {
    try {
      getApi().createReply(metaformId, payload, updateExisting, replyMode);
      fail(String.format("Expected create to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts update status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   * @param body           body payload
   * @param ownerKey       owner key
   */
  public void assertUpdateFailStatus(int expectedStatus, UUID metaformId, Reply body, String ownerKey) throws IOException {
    try {
      getApi().updateReply(metaformId, body.getId(), body, ownerKey);
      fail(String.format("Expected update to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts delete status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   * @param reply          reply
   * @param ownerKey       owner key
   */
  public void assertDeleteFailStatus(int expectedStatus, UUID metaformId, Reply reply, String ownerKey) throws IOException {
    try {
      getApi().deleteReply(metaformId, reply.getId(), ownerKey);
      fail(String.format("Expected delete to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts list status fails with given status code
   *
   * @param expectedStatus   expected status code
   * @param metaformId       Metaform id (required)
   * @param userId           Filter results by user id. If this parameter is not specified all replies are returned, this requires logged user to have proper permission to do so (optional)
   * @param createdBefore    Filter results created before specified time (optional)
   * @param createdAfter     Filter results created after specified time (optional)
   * @param modifiedBefore   Filter results modified before specified time (optional)
   * @param modifiedAfter    Filter results modified after specified time (optional)
   * @param includeRevisions Specifies that revisions should be included into response (optional)
   * @param fields           Filter results by field values. Format is field:value, multiple values can be added by using comma separator. E.g. field1&#x3D;value,field2&#x3D;another (optional)
   * @param firstResult      First index of results to be returned (optional)
   * @param maxResults       How many items to return at one time (optional)
   */
  public void assertListFailStatus(int expectedStatus, UUID metaformId, UUID userId, String createdBefore, String createdAfter, String modifiedBefore, String modifiedAfter, Boolean includeRevisions, String[] fields, Integer firstResult, Integer maxResults) throws IOException {
    try {
      getApi().listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults);
      fail(String.format("Expected list to fail with status %d", expectedStatus));
    } catch (ClientException e) {
      assertEquals(expectedStatus, e.getStatusCode());
    }
  }

  /**
   * Asserts that actual reply equals expected reply when both are serialized into JSON
   *
   * @param expected expected reply
   * @param actual   actual reply
   * @throws JSONException thrown when JSON serialization error occurs
   * @throws IOException   thrown when IO Exception occurs
   */
  public void assertRepliesEqual(Reply expected, Reply actual) throws IOException, JSONException {
    assertJsonsEqual(expected, actual);
  }

  /**
   * Creates a reply object with given data
   *
   * @param replyData reply data
   * @return reply object with given data
   */
  public Reply createReplyWithData(Map<String, Object> replyData) {
    return new Reply(null, null, null, null, null, null, replyData);
  }

  /**
   * Asserts that table datas equal
   *
   * @param expected expected table data
   * @param actual   actual table data
   */
  public void assertTableDataEquals(Map<String, Object> expected, Map<String, Object> actual) {
    assertNotNull(actual.get("table"));

    @SuppressWarnings("unchecked") List<Map<String, Object>> expectedTableData = (List<Map<String, Object>>) expected.get("table");
    @SuppressWarnings("unchecked") List<Map<String, Object>> actualTableData = (List<Map<String, Object>>) actual.get("table");

    assertEquals(expectedTableData.size(), actualTableData.size());

    for (Map<String, Object> expectedRow : expectedTableData) {
      for (Map.Entry<String, Object> expectedCell : expectedRow.entrySet()) {
        assertEquals(expectedCell.getValue(), expectedRow.get(expectedCell.getKey()));
      }
    }
  }

  /**
   * Creates permission select reply with given value
   *
   * @param value value
   * @return permission select reply with given value
   */
  public Reply createPermisionSelectReply(String value) {
    Map<String, Object> replyData = createPermissionSelectReplyData(value);
    return createReplyWithData(replyData);
  }

  /**
   * Creates permission select reply data with given value
   *
   * @param value value
   * @return permission select reply data with given value
   */
  public Map<String, Object> createPermissionSelectReplyData(String value) {
    Map<String, Object> replyData = new HashMap<>();
    replyData.put("permission-select", value);
    return replyData;
  }
}