package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.builder.resources.PdfRendererResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.awaitility.Awaitility
import java.util.concurrent.TimeUnit

/**
 * Tests that test the scripts
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(PdfRendererResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ScriptTestsIT : AbstractTest() {
    /*
  @Test
  public void testCreateReplyScript() throws Exception {
    waitThemeFlush();
    WireMock.resetAllRequests();

    UrlPattern externalMockURL = urlEqualTo("/externalmock");
    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));

    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.systemAdmin.metaforms().readMetaform("simple-script");
      Metaform metaform = testBuilder.systemAdmin.metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply replyWithData = testBuilder.test1.replies().createReplyWithData(replyData);
      testBuilder.test1.replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

      verify(1, postRequestedFor(externalMockURL).withRequestBody(containing("Text value: Test text value")));

    } finally {
      removeStub(externalStub);
    }
  }

  @Test
  public void testPdfScript() throws Exception {
    waitThemeFlush();
    WireMock.configureFor(8081);
    WireMock.resetAllRequests();

    UrlPattern externalMockURL = urlEqualTo("/externalmock");

    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));

    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.systemAdmin.metaforms().readMetaform("simple-pdf-script");
      Metaform metaform = testBuilder.systemAdmin.metaforms().create(parsedMetaform);

      ExportTheme theme = testBuilder.systemAdmin.exportThemes().createSimpleExportTheme();
      testBuilder.systemAdmin.exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>text: ${reply.getData()['text']!''}</body></html>");

      Metaform newMetaform = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(), metaform.getAllowDrafts(), metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(),
        metaform.getAutosave(), metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());
      testBuilder.systemAdmin.metaforms().updateMetaform(newMetaform.getId(), newMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "PDF text value");
      Reply replyWithData = testBuilder.test1.replies().createReplyWithData(replyData);
      testBuilder.test1.replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

      List<ServeEvent> serveEvents = WireMock.getAllServeEvents();
      assertEquals(1, serveEvents.size());

      assertPdfContains("PDF text value", serveEvents.get(0).getRequest().getBody());
    } finally {
      removeStub(externalStub);
    }
  }

  @Test
  public void testPdfBase64Script() throws Exception {
    waitThemeFlush();
    WireMock.configureFor(8081);
    WireMock.resetAllRequests();

    UrlPattern externalMockURL = urlEqualTo("/externalmock");

    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));

    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.systemAdmin.metaforms().readMetaform("simple-pdf-base64-script");
      Metaform metaform = testBuilder.systemAdmin.metaforms().create(parsedMetaform);

      ExportTheme theme = testBuilder.systemAdmin.exportThemes().createSimpleExportTheme();
      testBuilder.systemAdmin.exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>text: ${reply.getData()['text']!''}</body></html>");
      Metaform newMetaform = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(), metaform.getAllowDrafts(), metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(),
        metaform.getAutosave(), metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());
      testBuilder.systemAdmin.metaforms().updateMetaform(newMetaform.getId(), newMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "PDF text value");
      Reply replyWithData = testBuilder.test1.replies().createReplyWithData(replyData);
      testBuilder.test1.replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

      List<ServeEvent> serveEvents = WireMock.getAllServeEvents();
      assertEquals(1, serveEvents.size());

      assertPdfContains("PDF text value", Base64.getDecoder().decode(serveEvents.get(0).getRequest().getBody()));
    } finally {
      removeStub(externalStub);
    }
  }
 */

    /**
     * Wait until theme file caches are flushed
     */
    private fun waitThemeFlush() {
        Awaitility.await().atMost(1, TimeUnit.MINUTES).until { true }
    }
}