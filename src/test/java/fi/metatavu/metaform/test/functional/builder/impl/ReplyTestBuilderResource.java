package fi.metatavu.metaform.test.functional.builder.impl;

import feign.FeignException;
import fi.metatavu.metaform.client.ApiClient;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.model.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test builder resource for replies
 *
 * @author Antti Leppä
 */
public class ReplyTestBuilderResource extends ApiTestBuilderResource<Reply, RepliesApi> {

  private Map<UUID, UUID> replyMetaformIds = new HashMap<>();

  /**
   * Constructor
   *
   * @param testBuilder test builder
   * @param apiClient   initialized API client
   */
  public ReplyTestBuilderResource(TestBuilder testBuilder, ApiClient apiClient) {
    super(testBuilder, apiClient);
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
  public Reply create(UUID metaformId, Boolean updateExisting, String replyMode, Reply payload) {
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
  public Reply create(UUID metaformId, String replyMode, Reply payload) {
    return create(metaformId, null, replyMode, payload);
  }

  /**
   * Creates new reply for the simple form
   *
   * @param metaform metaform
   * @param value    value
   * @return reply
   */
  public Reply createSimpleReply(Metaform metaform, String value) {
    return createSimpleReply(metaform, value, ReplyMode.REVISION);
  }

  /**
   * Creates new reply for the simple form
   *
   * @param metaform  metaform
   * @param value     value
   * @param replyMode reply model
   * @return reply
   */
  public Reply createSimpleReply(Metaform metaform, String value, ReplyMode replyMode) {
    Map<String, Object> replyData1 = new HashMap<>();
    replyData1.put("text", value);
    Reply reply = createReplyWithData(replyData1);
    return create(metaform.getId(), replyMode.toString(), reply);
  }

  /**
   * Finds a reply
   *
   * @param metaformId metaform id
   * @param replyId    reply id
   * @param ownerKey   owner key
   * @return found reply
   */
  public Reply findReply(UUID metaformId, UUID replyId, String ownerKey) {
    return getApi().findReply(metaformId, replyId, ownerKey);
  }

  /**
   * Updates a reply into the API
   *
   * @param metaformId metaform id
   * @param body       body payload
   * @param ownerKey   owner key
   */
  public void updateReply(UUID metaformId, Reply body, String ownerKey) {
    getApi().updateReply(metaformId, body.getId(), body, ownerKey);
  }

  /**
   * Deletes a reply from the API
   *
   * @param metaformId metaform id
   * @param reply      reply to be deleted
   * @param ownerKey   owner key
   */
  public void delete(UUID metaformId, Reply reply, String ownerKey) {
    assertNotNull(reply.getId());
    getApi().deleteReply(metaformId, reply.getId(), ownerKey);
    removeCloseable(closable -> {
      if (closable instanceof Reply) {
        return !reply.getId().equals(((Reply) closable).getId());
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
  public void assertCount(int expected, UUID metaformId, UUID userId, String createdBefore, String createdAfter, String modifiedBefore, String modifiedAfter, Boolean includeRevisions, List<String> fields, Integer firstResult, Integer maxResults) {
    assertEquals(expected, getApi().listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults).size());
  }

  /**
   * Asserts find status fails with given status code
   *
   * @param expectedStatus expected status code
   * @param metaformId     metaform id
   * @param replyId        reply id
   * @param ownerKey       owner key
   */
  public void assertFindFailStatus(int expectedStatus, UUID metaformId, UUID replyId, String ownerKey) {
    try {
      getApi().findReply(metaformId, replyId, ownerKey);
      fail(String.format("Expected find to fail with status %d", expectedStatus));
    } catch (FeignException e) {
      assertEquals(expectedStatus, e.status());
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
  public void assertCreateFailStatus(int expectedStatus, UUID metaformId, Boolean updateExisting, String replyMode, Reply payload) {
    try {
      getApi().createReply(metaformId, payload, updateExisting, replyMode);
      fail(String.format("Expected create to fail with status %d", expectedStatus));
    } catch (FeignException e) {
      assertEquals(expectedStatus, e.status());
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
  public void assertUpdateFailStatus(int expectedStatus, UUID metaformId, Reply body, String ownerKey) {
    try {
      getApi().updateReply(metaformId, body.getId(), body, ownerKey);
      fail(String.format("Expected update to fail with status %d", expectedStatus));
    } catch (FeignException e) {
      assertEquals(expectedStatus, e.status());
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
  public void assertDeleteFailStatus(int expectedStatus, UUID metaformId, Reply reply, String ownerKey) {
    try {
      getApi().deleteReply(metaformId, reply.getId(), ownerKey);
      fail(String.format("Expected delete to fail with status %d", expectedStatus));
    } catch (FeignException e) {
      assertEquals(expectedStatus, e.status());
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
  public void assertListFailStatus(int expectedStatus, UUID metaformId, UUID userId, String createdBefore, String createdAfter, String modifiedBefore, String modifiedAfter, Boolean includeRevisions, List<String> fields, Integer firstResult, Integer maxResults) {
    try {
      getApi().listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults);
      fail(String.format("Expected list to fail with status %d", expectedStatus));
    } catch (FeignException e) {
      assertEquals(expectedStatus, e.status());
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
  private Reply createReplyWithData(Map<String, Object> replyData) {
    Reply reply = new Reply();
    reply.setData(replyData);
    return reply;
  }

  @Override
  public void clean(Reply reply) {
    UUID metaformId = replyMetaformIds.get(reply.getId());
    getApi().deleteReply(metaformId, reply.getId(), (String) null);
  }

}