package fi.metatavu.metaform.test.functional;

import feign.FeignException;
import fi.metatavu.metaform.client.api.*;
import fi.metatavu.metaform.client.model.*;
import fi.metatavu.metaform.server.rest.ReplyMode;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for AuditLogEntriesApi
 *
 * @author Katja Danilova
 */
public class AuditLogEntryTestsIT extends AbstractIntegrationTest {

	@Test
	public void basicActionsOnReplyTest() throws IOException{
	  String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
		RepliesApi repliesApi = getRepliesApi(accessToken);
		AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(accessToken);
		MetaformsApi adminMetaformsApi = getMetaformsApi(getAdminToken(REALM_1));

		Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));

		Map<String, Object> replyData = new HashMap<>();
		replyData.put("text", "Test text value");
		Reply reply = createReplyWithData(replyData);
		try {
			Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
			repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
			repliesApi.listReplies(metaform.getId(), null, null,null, null, null, true, null, null, null);
			repliesApi.updateReply(metaform.getId(), createdReply.getId(), createdReply, reply.getOwnerKey());
			repliesApi.deleteReply(metaform.getId(), createdReply.getId(), reply.getOwnerKey());

			List<AuditLogEntry> auditLogEntries = auditLogEntriesApi.listAuditLogEntries(metaform.getId(), null, createdReply.getId(), null, null);

			assertEquals("user "+REALM1_USER_1_ID+" created reply "+createdReply.getId(), auditLogEntries.get(0).getMessage() );
			assertEquals("user "+REALM1_USER_1_ID+" viewed reply "+createdReply.getId(), auditLogEntries.get(1).getMessage());
			assertEquals("user "+REALM1_USER_1_ID+" listed reply "+createdReply.getId(), auditLogEntries.get(2).getMessage());
			assertEquals("user "+REALM1_USER_1_ID+" modified reply "+createdReply.getId(), auditLogEntries.get(3).getMessage());
			assertEquals("user "+REALM1_USER_1_ID+" deleted reply "+createdReply.getId(), auditLogEntries.get(4).getMessage());

		} finally {
			adminMetaformsApi.deleteMetaform(metaform.getId());
		}
	}

	@Test
	public void queryByUserTest() throws Exception {
		String user1token = getAccessToken(REALM_1, "test1.realm1", "test");
		String user2token = getAccessToken(REALM_1, "test2.realm1", "test");
		RepliesApi repliesApiUser1 = getRepliesApi(user1token);

		RepliesApi repliesApiUser2 = getRepliesApi(user2token);

		AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(user1token);
		MetaformsApi adminMetaformsApi = getMetaformsApi(getAdminToken(REALM_1));

		Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));
		Map<String, Object> replyData = new HashMap<>();
		replyData.put("text", "Test text value");
		Reply reply = createReplyWithData(replyData);

		try {
			// test 1 creates reply
			Reply createdReply1 = repliesApiUser1.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
			// test 2 creates reply
			Reply createdReply2 = repliesApiUser2.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());

			List<AuditLogEntry> auditLogEntriesForUser1 = auditLogEntriesApi.listAuditLogEntries(metaform.getId(), REALM1_USER_1_ID, null, null, null);
			List<AuditLogEntry>	auditLogEntriesForUser2 = auditLogEntriesApi.listAuditLogEntries(metaform.getId(), REALM1_USER_2_ID, null, null, null);

			assertEquals("user " + REALM1_USER_1_ID + " created reply " + createdReply1.getId(), auditLogEntriesForUser1.get(0).getMessage());
			assertEquals("user " + REALM1_USER_2_ID + " created reply " + createdReply2.getId(), auditLogEntriesForUser2.get(0).getMessage());
		} finally {
			adminMetaformsApi.deleteMetaform(metaform.getId());
		}
	}

	@Test
	public void queryByMetaformTest() throws Exception {
		String user1token = getAccessToken(REALM_1, "test1.realm1", "test");
		RepliesApi repliesApi = getRepliesApi(user1token);

		AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(user1token);
		MetaformsApi adminMetaformsApi = getMetaformsApi(getAdminToken(REALM_1));

		Metaform metaform1 = adminMetaformsApi.createMetaform(readMetaform("simple"));
		Metaform metaform2 = adminMetaformsApi.createMetaform(readMetaform("simple"));

		Map<String, Object> replyData = new HashMap<>();
		replyData.put("text", "Test text value");
		Reply reply = createReplyWithData(replyData);

		try {
			Reply createdReply1 = repliesApi.createReply(metaform1.getId(), reply, null, ReplyMode.REVISION.toString());
			Reply createdReply2 = repliesApi.createReply(metaform2.getId(), reply, null, ReplyMode.REVISION.toString());

			List<AuditLogEntry> metaform1AuditLogs = auditLogEntriesApi.listAuditLogEntries(metaform1.getId(), null, null, null, null);
			List<AuditLogEntry>	metaform2AuditLogs = auditLogEntriesApi.listAuditLogEntries(metaform2.getId(), null, null, null, null);

			assertEquals(1, metaform1AuditLogs.size());
			assertEquals(1, metaform2AuditLogs.size());
			assertEquals("user " + REALM1_USER_1_ID + " created reply " + createdReply1.getId(), metaform1AuditLogs.get(0).getMessage());
			assertEquals("user " + REALM1_USER_1_ID + " created reply " + createdReply2.getId(), metaform2AuditLogs.get(0).getMessage());
		} finally {
			adminMetaformsApi.deleteMetaform(metaform1.getId());
			adminMetaformsApi.deleteMetaform(metaform2.getId());
		}
	}

	/**
	 * test verifies that sorting by reply id words
	 * @throws IOException
	 */
	@Test
	public void queryByReplyIdTest() throws IOException {
		String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
		RepliesApi repliesApi = getRepliesApi(accessToken);
		AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(accessToken);
		MetaformsApi adminMetaformsApi = getMetaformsApi(getAdminToken(REALM_1));

		Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));

		Map<String, Object> replyData = new HashMap<>();
		replyData.put("text", "Test text value");
		Reply reply = createReplyWithData(replyData);
		Reply reply2 = createReplyWithData(replyData);
		try {
			Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
			repliesApi.createReply(metaform.getId(), reply2, null, ReplyMode.REVISION.toString());

			List<AuditLogEntry> auditLogEntries = auditLogEntriesApi.listAuditLogEntries(metaform.getId(), null, null, null, null);
			assertEquals(2, auditLogEntries.size());

			List<AuditLogEntry> entryByReply = auditLogEntriesApi.listAuditLogEntries(metaform.getId(), null, createdReply.getId(), null, null);
			assertEquals(1, entryByReply.size());
			assertEquals("user b6039e55-3758-4252-9858-a973b0988b63 created reply "+createdReply.getId(), entryByReply.get(0).getMessage());

		} finally {
			adminMetaformsApi.deleteMetaform(metaform.getId());
		}
	}

	/**
     * Tests accessing the audit logs
     * @throws IOException
     */
    @Test
		public void accessRightsTest() throws IOException {
    	String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    	String accessToken1 = getAccessToken(REALM_1, "test2.realm1", "test");
    	String adminToken = getAdminToken(REALM_1);

    	MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    	RepliesApi repliesApi = getRepliesApi(accessToken);
    	AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(accessToken);
    	AuditLogEntriesApi auditLogEntriesApi1 = getAuditLogEntriesApi(accessToken1);

    	Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));
			List<AuditLogEntry> auditLogEntries = null;
    	try {
    		//reply to metaform
				Map<String, Object> replyData = new HashMap<>();
				replyData.put("text", "Test text value");
				Reply reply = createReplyWithData(replyData);
				repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());

				auditLogEntries = auditLogEntriesApi.listAuditLogEntries(metaform.getId(), null, null, null, null);
				assertNotNull(auditLogEntries);
				try {
					auditLogEntriesApi1.listAuditLogEntries(metaform.getId(), null, null, null, null);
					fail(String.format("Only users with metaform-view-all-audit-logs can access this view"));
				} catch (FeignException e) {
					assertEquals(403, e.status());
				}
    	}
    	finally {
				adminMetaformsApi.deleteMetaform(metaform.getId());
    	}
    }
}
