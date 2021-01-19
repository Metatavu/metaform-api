package fi.metatavu.metaform.test.functional;

import fi.metatavu.metaform.client.api.AuditLogEntriesApi;
import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.AuditLogEntry;
import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.model.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class AuditLogEntryTestsIT extends AbstractIntegrationTest {


    /**
     * test creation of audit logs for reply creation, viewing and deleting
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void createAuditEntry() throws IOException, URISyntaxException {

        String adminToken = getAdminToken(REALM_1);
        String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
        MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
        RepliesApi repliesApi = getRepliesApi(accessToken);
        AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(accessToken);

        Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));

        try {
            Map<String, Object> replyData = new HashMap<>();
            replyData.put("text", "Test text value");
            Reply reply = createReplyWithData(replyData);

            //do actions on the reply
            Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
            Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);

            List<AuditLogEntry> auditLogEntries = auditLogEntriesApi.findAuditLogEntries(metaform.getId(), null, null, null, null, null);

            assertNotNull(auditLogEntries);
            assertEquals(auditLogEntries.get(0).getLogEntryType(), AuditLogEntryType.CREATE_REPLY);
            assertEquals(auditLogEntries.get(1).getLogEntryType(), AuditLogEntryType.VIEW_REPLY);

        } finally {
            adminMetaformsApi.deleteMetaform(metaform.getId());
        }


    }



}
