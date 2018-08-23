package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;

@SuppressWarnings ("squid:S1192")
public class MetaformTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void testCreateMetaform() throws IOException, URISyntaxException {
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
      Metaform foundMetaform = adminMetaformsApi.findMetaform(REALM_1, metaform.getId());
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
      
      assertEquals(0, adminMetaformsApi.listMetaforms(REALM_1).size());
      
      Metaform metaform1 = dataBuilder.createMetaform("simple");
      Metaform metaform2 = dataBuilder.createMetaform("simple");

      List<Metaform> list = adminMetaformsApi.listMetaforms(REALM_1);

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
      Metaform updatedMetaform = adminMetaformsApi.updateMetaform(REALM_1, metaform.getId(), updatePayload);
      
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
