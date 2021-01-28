package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.MetaformsApi;

@SuppressWarnings ("squid:S1192")
public class MetaformTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void testCreateMetaform() throws IOException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple");

      assertNotNull(metaform);
      assertNotNull(metaform.getId());
      assertEquals("Simple", metaform.getTitle());
      assertEquals(1, metaform.getSections().size());
      assertEquals("Simple form", metaform.getSections().get(0).getTitle());
      assertEquals(1, metaform.getSections().get(0).getFields().size());
      assertEquals("text", metaform.getSections().get(0).getFields().get(0).getName());
      assertEquals("text", metaform.getSections().get(0).getFields().get(0).getType().toString());
      assertEquals("Text field", metaform.getSections().get(0).getFields().get(0).getTitle());
      assertEquals(true, metaform.getAllowDrafts());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testCreateMetaformScript() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple-script");

      assertNotNull(metaform);
      assertNotNull(metaform.getId());
      assertNotNull(metaform.getScripts());
      assertNotNull(metaform.getScripts().getAfterCreateReply());
      assertEquals(2, metaform.getScripts().getAfterCreateReply().size());
      assertEquals("create-test", metaform.getScripts().getAfterCreateReply().get(0).getName());
      assertEquals("js", metaform.getScripts().getAfterCreateReply().get(0).getLanguage());
      assertEquals("form.setVariableValue('postdata', 'Text value: ' + form.getReplyData().get('text'));", metaform.getScripts().getAfterCreateReply().get(0).getContent());
      assertNotNull(metaform.getScripts().getAfterUpdateReply());
      assertEquals("update-test", metaform.getScripts().getAfterUpdateReply().get(0).getName());
      assertEquals("js", metaform.getScripts().getAfterUpdateReply().get(0).getLanguage());
      assertEquals("const xhr = new XMLHttpRequest(); xhr.open('GET', 'http://test-wiremock:8080/externalmock'); xhr.send();", metaform.getScripts().getAfterUpdateReply().get(0).getContent());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testFindMetaform() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MetaformsApi adminMetaformsApi = dataBuilder.getAdminMetaformsApi();
      Metaform metaform = dataBuilder.createMetaform("simple");
      Metaform foundMetaform = adminMetaformsApi.findMetaform(metaform.getId(), null, null);
      assertEquals(metaform.toString(), foundMetaform.toString());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testListMetaform() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MetaformsApi adminMetaformsApi = dataBuilder.getAdminMetaformsApi();
      
      assertEquals(0, adminMetaformsApi.listMetaforms().size());
      
      Metaform metaform1 = dataBuilder.createMetaform("simple");
      Metaform metaform2 = dataBuilder.createMetaform("simple");
      
      metaform1.setTitle("first");
      metaform2.setTitle("second");
      
      adminMetaformsApi.updateMetaform(metaform1.getId(), metaform1);
      adminMetaformsApi.updateMetaform(metaform2.getId(), metaform2);

      List<Metaform> list = adminMetaformsApi.listMetaforms();
      
      list.sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));

      assertEquals(metaform1.toString(), list.get(0).toString());
      assertEquals(metaform2.toString(), list.get(1).toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testUpdateMetaform() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MetaformsApi adminMetaformsApi = dataBuilder.getAdminMetaformsApi();      
      Metaform metaform = dataBuilder.createMetaform("simple");
      
      Metaform updatePayload = readMetaform("tbnc");
      Metaform updatedMetaform = adminMetaformsApi.updateMetaform(metaform.getId(), updatePayload);
      
      assertEquals(metaform.getId(), updatedMetaform.getId());
      assertEquals(1, updatedMetaform.getSections().size());
      assertEquals("Text, boolean, number, checklist form", updatedMetaform.getSections().get(0).getTitle());
      assertEquals(4, updatedMetaform.getSections().get(0).getFields().size());
      assertEquals("text", updatedMetaform.getSections().get(0).getFields().get(0).getName());
      assertEquals("boolean", updatedMetaform.getSections().get(0).getFields().get(1).getName());
      assertEquals("number", updatedMetaform.getSections().get(0).getFields().get(2).getName());
      assertEquals("checklist", updatedMetaform.getSections().get(0).getFields().get(3).getName());
    } finally {
      dataBuilder.clean();
    }
  }

}
