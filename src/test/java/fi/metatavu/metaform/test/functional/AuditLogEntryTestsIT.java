package fi.metatavu.metaform.test.functional;

import feign.FeignException;
import fi.metatavu.metaform.client.api.*;
import fi.metatavu.metaform.client.model.*;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;


import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class AuditLogEntryTestsIT extends AbstractIntegrationTest {


    /**
     * test creation of audit logs for reply creation, viewing and deleting
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void createReplyAuditEntry() throws IOException{
        String adminToken = getAdminToken(REALM_1);
        String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
        MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
        RepliesApi repliesApi = getRepliesApi(accessToken);
        AuditLogEntriesApi auditLogEntriesApi = getAuditLogEntriesApi(accessToken);
        TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");

        Metaform metaform = dataBuilder.createMetaform("files");

        ExportTheme theme = dataBuilder.createSimpleExportTheme();
        dataBuilder.createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>");
        metaform.setExportThemeId(theme.getId());
        adminMetaformsApi.updateMetaform(metaform.getId(), metaform);

        FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
        FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");

        String fileRef1 = fileUpload1.getFileRef().toString();
        String fileRef2 = fileUpload2.getFileRef().toString();
        List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);

        Map<String, Object> replyData = new HashMap<>();
        replyData.put("files", fileRefs);
        Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);

        try {
            repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
            repliesApi.listReplies(metaform.getId(), null, null,null, null, null, null, null, null, null);
            repliesApi.updateReply(metaform.getId(), reply.getId(), reply, reply.getOwnerKey());

           // repliesApi.replyExport(metaform.getId(), reply.getId(), "PDF");
            given()
                    .baseUri(getBasePath())
                    .header("Authorization", String.format("Bearer %s", accessToken))
                    .get("/v1/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", metaform.getId(), reply.getId())
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .header("Content-Type", "application/pdf");

            List<AuditLogEntry> auditLogEntries = auditLogEntriesApi.findAuditLogEntries(metaform.getId(), null, null, null, null, null);

            assertNotNull(auditLogEntries);
            assertEquals(auditLogEntries.get(0).getMessage(),reply.getId()+" was created by user b6039e55-3758-4252-9858-a973b0988b63");
            assertEquals(auditLogEntries.get(1).getMessage(),reply.getId()+" was viewed by user b6039e55-3758-4252-9858-a973b0988b63");
            assertEquals(auditLogEntries.get(2).getMessage(),reply.getId()+" was viewed by user b6039e55-3758-4252-9858-a973b0988b63");
            assertEquals(auditLogEntries.get(3).getMessage(),reply.getId()+" was updated by user b6039e55-3758-4252-9858-a973b0988b63");
            assertEquals(auditLogEntries.get(4).getMessage(),reply.getId()+" was exported to pdf by user b6039e55-3758-4252-9858-a973b0988b63");
            assertEquals(auditLogEntries.get(5).getMessage(),reply.getId()+" was exported to pdf by user b6039e55-3758-4252-9858-a973b0988b63");


        } finally {
            dataBuilder.clean();
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

        try {
            //reply to metaform
            Map<String, Object> replyData = new HashMap<>();
            replyData.put("text", "Test text value");
            Reply reply = createReplyWithData(replyData);
            repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());

            List<AuditLogEntry> auditLogEntries = auditLogEntriesApi.findAuditLogEntries(metaform.getId(), null, null, null, null, null);
            assertNotNull(auditLogEntries);

            try {
                auditLogEntriesApi1.findAuditLogEntries(metaform.getId(), null, null, null, null, null);
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
