package fi.metatavu.metaform.test.functional.tests;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import fi.metatavu.metaform.api.client.models.ExportTheme;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(DefTestProfile.class)
public class ScriptTestsIT extends AbstractIntegrationTest {

  @Test
  public void testCreateReplyScript() throws Exception {
    waitThemeFlush();
    WireMock.resetAllRequests();

    UrlPattern externalMockURL = urlEqualTo("/externalmock");
    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));

    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-script");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply replyWithData = testBuilder.test1().replies().createReplyWithData(replyData);
      testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

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
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-pdf-script");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      ExportTheme theme = testBuilder.metaformSuper().exportThemes().createSimpleExportTheme();
      testBuilder.metaformSuper().exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>text: ${reply.getData()['text']!''}</body></html>");

      Metaform newMetaform = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(), metaform.getAllowDrafts(), metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(),
        metaform.getAutosave(), metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());
      testBuilder.metaformAdmin().metaforms().updateMetaform(newMetaform.getId(), newMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "PDF text value");
      Reply replyWithData = testBuilder.test1().replies().createReplyWithData(replyData);
      testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

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
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-pdf-base64-script");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      ExportTheme theme = testBuilder.metaformSuper().exportThemes().createSimpleExportTheme();
      testBuilder.metaformSuper().exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>text: ${reply.getData()['text']!''}</body></html>");
      Metaform newMetaform = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(), metaform.getAllowDrafts(), metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(),
        metaform.getAutosave(), metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());
      testBuilder.metaformAdmin().metaforms().updateMetaform(newMetaform.getId(), newMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "PDF text value");
      Reply replyWithData = testBuilder.test1().replies().createReplyWithData(replyData);
      testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

      List<ServeEvent> serveEvents = WireMock.getAllServeEvents();
      assertEquals(1, serveEvents.size());

      assertPdfContains("PDF text value", Base64.getDecoder().decode(serveEvents.get(0).getRequest().getBody()));
    } finally {
      removeStub(externalStub);
    }
  }

  /**
   * Asserts that given PDF data contains expected string
   *
   * @param expected expected string
   * @param data     PDF data
   * @throws IOException thrown on PDF read failure
   */
  protected void assertPdfContains(String expected, byte[] data) throws IOException {
    PDDocument document = PDDocument.load(new ByteArrayInputStream(data));
    String pdfText = new PDFTextStripper().getText(document);
    document.close();

    assertTrue(String.format("PDF text (%s) does not contain expected text %s", pdfText, expected), StringUtils.contains(pdfText, expected));
  }

  /**
   * Wait until theme file caches are flushed
   */
  private void waitThemeFlush() {
    await().atMost(1, TimeUnit.MINUTES).until(() -> true);
  }
}
