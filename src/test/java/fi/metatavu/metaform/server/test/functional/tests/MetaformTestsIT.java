package fi.metatavu.metaform.server.test.functional.tests;


import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource;
import fi.metatavu.metaform.server.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(GeneralTestProfile.class)
public class MetaformTestsIT extends AbstractIntegrationTest {

  @Test
  public void testCreateMetaform() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform1 = builder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform1 = builder.metaformAdmin.metaforms().create(parsedMetaform1);

      assertNotNull(metaform1);
      assertNotNull(metaform1.getId());
      assertNotNull(metaform1.getSlug());
      assertEquals("Simple", metaform1.getTitle());
      assertEquals(1, metaform1.getSections().length);
      assertEquals("Simple form", metaform1.getSections()[0].getTitle());
      assertEquals(1, metaform1.getSections()[0].getFields().length);
      assertEquals("text", metaform1.getSections()[0].getFields()[0].getName());
      assertEquals("text", metaform1.getSections()[0].getFields()[0].getType().toString());
      assertEquals("Text field", metaform1.getSections()[0].getFields()[0].getTitle());
      assertEquals(true, metaform1.getAllowDrafts());

      Metaform parsedMetaform2 = builder.metaformAdmin.metaforms().readMetaform("simple-slug");
      Metaform metaform2 = builder.metaformAdmin.metaforms().create(parsedMetaform2);

      assertNotNull(metaform2);
      assertNotNull(metaform2.getId());
      assertEquals("Simple", metaform2.getTitle());
      assertEquals("simple-slug-0", metaform2.getSlug());
      assertEquals(1, metaform2.getSections().length);
      assertEquals("Simple form", metaform2.getSections()[0].getTitle());
      assertEquals(1, metaform2.getSections()[0].getFields().length);
      assertEquals("text", metaform2.getSections()[0].getFields()[0].getName());
      assertEquals("text", metaform2.getSections()[0].getFields()[0].getType().toString());
      assertEquals("Text field", metaform2.getSections()[0].getFields()[0].getTitle());
      assertEquals(true, metaform2.getAllowDrafts());
    }
  }

  @Test
  public void testCreateInvalidSlugMetaform() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin.metaforms().readMetaform("simple-slug-invalid");

      builder.metaformAdmin.metaforms().assertCreateFailStatus(409, parsedMetaform);
    }
  }

  @Test
  public void testCreateDuplicatedSlugMetaform() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform1 = builder.metaformAdmin.metaforms().readMetaform("simple-slug");
      Metaform metaform1 = builder.metaformAdmin.metaforms().create(parsedMetaform1);

      Metaform parsedMetaform2 = builder.metaformAdmin.metaforms().readMetaform("simple-slug");

      builder.metaformAdmin.metaforms().assertCreateFailStatus(409, parsedMetaform2);
    }
  }

  @Test
  public void testCreateMetaformScript() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin.metaforms().readMetaform("simple-script");
      Metaform metaform = builder.metaformAdmin.metaforms().create(parsedMetaform);

      assertNotNull(metaform);
      assertNotNull(metaform.getId());
      assertNotNull(metaform.getScripts());
      assertNotNull(metaform.getScripts().getAfterCreateReply());
      assertEquals(2, metaform.getScripts().getAfterCreateReply().length);
      assertEquals("create-test", metaform.getScripts().getAfterCreateReply()[0].getName());
      assertEquals("js", metaform.getScripts().getAfterCreateReply()[0].getLanguage());
      assertEquals("form.setVariableValue('postdata', 'Text value: ' + form.getReplyData().get('text'));", metaform.getScripts().getAfterCreateReply()[0].getContent());
      assertNotNull(metaform.getScripts().getAfterUpdateReply());
      assertEquals("update-test", metaform.getScripts().getAfterUpdateReply()[0].getName());
      assertEquals("js", metaform.getScripts().getAfterUpdateReply()[0].getLanguage());
      assertEquals("const xhr = new XMLHttpRequest(); xhr.open('GET', 'http://test-wiremock:8080/externalmock'); xhr.send();", metaform.getScripts().getAfterUpdateReply()[0].getContent());
    }
  }

  @Test
  public void testFindMetaform() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin.metaforms().create(parsedMetaform);

      Metaform foundMetaform = builder.metaformAdmin.metaforms().findMetaform(metaform.getId(), null, null);
      assertEquals(metaform.toString(), foundMetaform.toString());
    }
  }

  @Test
  public void testListMetaform() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      assertEquals(0, builder.metaformAdmin.metaforms().list().length);

      Metaform parsedMetaform1 = builder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform parsedMetaform2 = builder.metaformAdmin.metaforms().readMetaform("simple");

      Metaform metaform1 = builder.metaformAdmin.metaforms().create(parsedMetaform1);
      Metaform metaform2 = builder.metaformAdmin.metaforms().create(parsedMetaform2);

      Metaform metaform1Modified = new Metaform(metaform1.getId(), metaform1.getReplyStrategy(), metaform1.getExportThemeId(), metaform1.getAllowAnonymous(), metaform1.getAllowDrafts(),
        metaform1.getAllowReplyOwnerKeys(), metaform1.getAllowInvitations(), metaform1.getAutosave(), "first", "first-slug-0", metaform1.getSections(), metaform1.getFilters(),
        metaform1.getScripts());
      Metaform metaform2Modified = new Metaform(metaform2.getId(), metaform2.getReplyStrategy(), metaform2.getExportThemeId(), metaform2.getAllowAnonymous(), metaform2.getAllowDrafts(),
        metaform2.getAllowReplyOwnerKeys(), metaform2.getAllowInvitations(), metaform2.getAutosave(), "second", "second-slug-0", metaform2.getSections(), metaform2.getFilters(),
        metaform2.getScripts());

      builder.metaformAdmin.metaforms().updateMetaform(metaform1.getId(), metaform1Modified);
      builder.metaformAdmin.metaforms().updateMetaform(metaform2.getId(), metaform2Modified);

      List<Metaform> list = Arrays.asList(builder.metaformAdmin.metaforms().list().clone());

      list.sort(Comparator.comparing(Metaform::getTitle));

      assertEquals(metaform1Modified.toString(), list.get(0).toString());
      assertEquals(metaform2Modified.toString(), list.get(1).toString());
    }
  }

  @Test
  public void testUpdateMetaform() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform1 = builder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform1 = builder.metaformAdmin.metaforms().create(parsedMetaform1);

      Metaform updatePayload = builder.metaformAdmin.metaforms().readMetaform("tbnc");
      Metaform updatedMetaform = builder.metaformAdmin.metaforms().updateMetaform(metaform1.getId(), updatePayload);

      assertEquals(metaform1.getId(), updatedMetaform.getId());
      assertEquals(1, updatedMetaform.getSections().length);
      assertEquals("Text, boolean, number, checklist form", updatedMetaform.getSections()[0].getTitle());
      assertEquals(4, updatedMetaform.getSections()[0].getFields().length);
      assertEquals("text", updatedMetaform.getSections()[0].getFields()[0].getName());
      assertEquals("boolean", updatedMetaform.getSections()[0].getFields()[1].getName());
      assertEquals("number", updatedMetaform.getSections()[0].getFields()[2].getName());
      assertEquals("checklist", updatedMetaform.getSections()[0].getFields()[3].getName());
    }
  }

  @Test
  public void testUpdateMetaformNullSlug() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform1 = builder.metaformAdmin.metaforms().readMetaform("simple-slug");
      Metaform metaform1 = builder.metaformAdmin.metaforms().create(parsedMetaform1);

      Metaform updatePayload = builder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform updatedMetaform = builder.metaformAdmin.metaforms().updateMetaform(metaform1.getId(), updatePayload);

      assertEquals(metaform1.getSlug(), updatedMetaform.getSlug());
    }
  }

  @Test
  public void testUpdateMetaformDuplicatedSlug() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform1 = builder.metaformAdmin.metaforms().readMetaform("simple-slug");
      Metaform metaform1 = builder.metaformAdmin.metaforms().create(parsedMetaform1);

      Metaform parsedMetaform2 = builder.metaformAdmin.metaforms().readMetaform("simple-slug2");
      Metaform metaform2 = builder.metaformAdmin.metaforms().create(parsedMetaform2);

      Metaform updatePayload = builder.test1.metaforms().readMetaform("simple-slug2");

      builder.metaformAdmin.metaforms().assertUpdateFailStatus(409, metaform1.getId(), updatePayload);
    }
  }
}
