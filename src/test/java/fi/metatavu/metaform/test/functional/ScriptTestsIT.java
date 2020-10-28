package fi.metatavu.metaform.test.functional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.ExportTheme;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.model.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class ScriptTestsIT extends AbstractIntegrationTest {

  @Test
  public void testCreateReplyScript() throws IOException, URISyntaxException {
    waitThemeFlush();
    WireMock.resetAllRequests();
    
    UrlPattern externalMockURL = urlEqualTo("/externalmock");
    
    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));
    
    try {
      String adminToken = getAdminToken(REALM_1);
      
      RepliesApi repliesApi = getRepliesApi(adminToken);
      
      TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
      try {
        Metaform metaform = dataBuilder.createMetaform("simple-script");
      
        Map<String, Object> replyData = new HashMap<>();
        replyData.put("text", "Test text value");
        Reply reply = createReplyWithData(replyData);
        repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
        
        verify(1, postRequestedFor(externalMockURL).withRequestBody(containing("Text value: Test text value")));
      } finally {
        dataBuilder.clean();
      }
    } finally {
      removeStub(externalStub);
    }
  }

  @Test
  public void testPdfScript() throws IOException, URISyntaxException {
    waitThemeFlush();
    WireMock.resetAllRequests();
    
    UrlPattern externalMockURL = urlEqualTo("/externalmock");
    
    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));
    
    try {
      String adminToken = getAdminToken(REALM_1);

      RepliesApi repliesApi = getRepliesApi(adminToken);
      MetaformsApi metaformsApi = getMetaformsApi(adminToken);
      
      TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
      
      try {
        Metaform metaform = dataBuilder.createMetaform("simple-pdf-script");
        ExportTheme theme = dataBuilder.createSimpleExportTheme();
        dataBuilder.createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>text: ${reply.getData()['text']!''}</body></html>");
        metaform.setExportThemeId(theme.getId());
        metaformsApi.updateMetaform(metaform.getId(), metaform);
        
        Map<String, Object> replyData = new HashMap<>();
        replyData.put("text", "PDF text value");
        Reply reply = createReplyWithData(replyData);
        repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
        
        List<ServeEvent> serveEvents = WireMock.getAllServeEvents();
        assertEquals(1, serveEvents.size());
        
        assertPdfContains("PDF text value", serveEvents.get(0).getRequest().getBody());
      } finally {
        dataBuilder.clean();
      }
    } finally {
      removeStub(externalStub);
    }
  }

  @Test
  public void testPdfBase64Script() throws IOException, URISyntaxException {
    waitThemeFlush();
    WireMock.resetAllRequests();
    
    UrlPattern externalMockURL = urlEqualTo("/externalmock");
    
    StubMapping externalStub = stubFor(post(externalMockURL).willReturn(aResponse().withStatus(200)));
    
    try {
      String adminToken = getAdminToken(REALM_1);

      RepliesApi repliesApi = getRepliesApi(adminToken);
      MetaformsApi metaformsApi = getMetaformsApi(adminToken);
      
      TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
      
      try {
        Metaform metaform = dataBuilder.createMetaform("simple-pdf-base64-script");
        ExportTheme theme = dataBuilder.createSimpleExportTheme();
        dataBuilder.createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>text: ${reply.getData()['text']!''}</body></html>");
        metaform.setExportThemeId(theme.getId());
        metaformsApi.updateMetaform(metaform.getId(), metaform);
        
        Map<String, Object> replyData = new HashMap<>();
        replyData.put("text", "PDF text value");
        Reply reply = createReplyWithData(replyData);
        repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
        
        List<ServeEvent> serveEvents = WireMock.getAllServeEvents();
        assertEquals(1, serveEvents.size());
        
        assertPdfContains("PDF text value", Base64.getDecoder().decode(serveEvents.get(0).getRequest().getBody()));
      } finally {
        dataBuilder.clean();
      }
    } finally {
      removeStub(externalStub);
    }
  }

  /**
   * Wait until theme file caches are flushed
   */
  private void waitThemeFlush() {
    await().atMost(1, TimeUnit.MINUTES).until(() -> true);
  }
  
}
