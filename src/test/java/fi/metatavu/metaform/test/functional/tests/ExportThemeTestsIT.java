package fi.metatavu.metaform.test.functional.tests;


import fi.metatavu.metaform.api.client.models.ExportTheme;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(GeneralTestProfile.class)
public class ExportThemeTestsIT extends AbstractIntegrationTest {

  @Test
  public void createExportThemeTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {

      ExportTheme parentTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();

      ExportTheme childPayload = new ExportTheme("child theme", null, parentTheme.getId(), "locales");
      ExportTheme childTheme = testBuilder.metaformSuper.exportThemes().createExportTheme(childPayload);
      Assertions.assertNotNull(childTheme);

      ExportTheme foundChildTheme = testBuilder.metaformSuper.exportThemes().findExportTheme(childTheme.getId());

      Assertions.assertNotNull(foundChildTheme);
      Assertions.assertEquals(childTheme.toString(), foundChildTheme.toString());
      Assertions.assertEquals("child theme", foundChildTheme.getName());
      Assertions.assertEquals("locales", foundChildTheme.getLocales());
      Assertions.assertEquals(parentTheme.getId(), foundChildTheme.getParentId());
    }
  }

  @Test
  public void findExportThemeTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();

      Assertions.assertNotNull(exportTheme);
      Assertions.assertNotNull(exportTheme.getName());

      ExportTheme foundExportTheme = testBuilder.metaformSuper.exportThemes().findExportTheme(exportTheme.getId());

      Assertions.assertNotNull(foundExportTheme);
      Assertions.assertEquals(exportTheme.getName(), foundExportTheme.getName());
    }
  }

  @Test
  public void listExportThemeTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme1 = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme("theme 1");
      ExportTheme exportTheme2 = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme("theme 2");

      List<ExportTheme> exportThemes = testBuilder.metaformSuper.exportThemes().listExportThemes();

      Assertions.assertEquals(2, exportThemes.size());
      Assertions.assertEquals(exportTheme1.toString(), exportThemes.get(0).toString());
      Assertions.assertEquals(exportTheme2.toString(), exportThemes.get(1).toString());
    }
  }

  @Test
  public void updateExportThemeTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme parentTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme("not updated");

      Assertions.assertNotNull(exportTheme);
      Assertions.assertEquals("not updated", exportTheme.getName());
      Assertions.assertNull(exportTheme.getLocales());
      Assertions.assertNull(exportTheme.getParentId());

      ExportTheme newExportTheme = new ExportTheme("updated", null, parentTheme.getId(), "locales");
      ExportTheme updatedTheme = testBuilder.metaformSuper.exportThemes().updateExportTheme(exportTheme.getId(), newExportTheme);

      Assertions.assertNotNull(updatedTheme);
      Assertions.assertEquals("updated", updatedTheme.getName());
      Assertions.assertEquals("locales", updatedTheme.getLocales());
      Assertions.assertEquals(parentTheme.getId(), updatedTheme.getParentId());
    }
  }

  @Test
  public void deleteExportThemeTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme payload = new ExportTheme("to be deleted", null, null, null);
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createExportTheme(payload);

      Assertions.assertEquals(1, testBuilder.metaformSuper.exportThemes().listExportThemes().size());
      Assertions.assertEquals(testBuilder.metaformSuper.exportThemes().findExportTheme(exportTheme.getId()).toString(), exportTheme.toString());

      testBuilder.metaformSuper.exportThemes().deleteExportTheme(exportTheme);

      Assertions.assertEquals(0, testBuilder.metaformSuper.exportThemes().listExportThemes().size());
      testBuilder.metaformSuper.exportThemes().assertSearchFailStatus(404, exportTheme.getId());
    }
  }

  @Test
  public void createExportThemeFileTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      fi.metatavu.metaform.api.client.models.ExportThemeFile exportThemeFile = testBuilder.metaformSuper.exportfiles().createSimpleExportThemeFile(theme.getId(), "path/to/file", "file content");

      Assertions.assertNotNull(exportThemeFile);
      Assertions.assertEquals("path/to/file", exportThemeFile.getPath());
      Assertions.assertEquals("file content", exportThemeFile.getContent());
      Assertions.assertEquals(theme.getId(), exportThemeFile.getThemeId());
    }
  }

  @Test
  public void findExportThemeFileTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      fi.metatavu.metaform.api.client.models.ExportThemeFile createThemeFile = testBuilder.metaformSuper.exportfiles().createSimpleExportThemeFile(theme.getId(), "path/to/file", "file content 1");

      Assertions.assertNotNull(createThemeFile);

      fi.metatavu.metaform.api.client.models.ExportThemeFile foundFile = testBuilder.metaformSuper.exportfiles().findExportThemeFile(theme.getId(), createThemeFile.getId());

      Assertions.assertEquals(createThemeFile.toString(), foundFile.toString());
    }
  }

  @Test
  public void listExportThemeFileTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();

      Assertions.assertEquals(0, testBuilder.metaformSuper.exportfiles().listExportThemeFiles(theme.getId()).size());

      fi.metatavu.metaform.api.client.models.ExportThemeFile exportThemeFile1 =
        testBuilder.metaformSuper.exportfiles().createSimpleExportThemeFile(theme.getId(), "path/to/file1", "file content 1");
      fi.metatavu.metaform.api.client.models.ExportThemeFile exportThemeFile2
        = testBuilder.metaformSuper.exportfiles().createSimpleExportThemeFile(theme.getId(), "path/to/file2", "file content 2");

      List<fi.metatavu.metaform.api.client.models.ExportThemeFile> themeFiles = testBuilder.metaformSuper.exportfiles().listExportThemeFiles(theme.getId());

      Assertions.assertEquals(2, themeFiles.size());
      Assertions.assertEquals(exportThemeFile1.toString(), themeFiles.get(0).toString());
      Assertions.assertEquals(exportThemeFile2.toString(), themeFiles.get(1).toString());
    }
  }

  @Test
  public void updateExportThemeFileTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();

      fi.metatavu.metaform.api.client.models.ExportThemeFile createdThemeFile =
        testBuilder.metaformSuper.exportfiles().createSimpleExportThemeFile(theme.getId(), "not/updated", "not updated");

      Assertions.assertEquals("not/updated", createdThemeFile.getPath());
      Assertions.assertEquals("not updated", createdThemeFile.getContent());
      Assertions.assertEquals(theme.getId(), createdThemeFile.getThemeId());

      fi.metatavu.metaform.api.client.models.ExportThemeFile newThemeFile = new fi.metatavu.metaform.api.client.models.ExportThemeFile(
        "is/updated", createdThemeFile.getThemeId(), "is updated", createdThemeFile.getId());

      fi.metatavu.metaform.api.client.models.ExportThemeFile updatedThemeFile = testBuilder.metaformSuper.exportfiles().updateExportThemeFile(theme.getId(), createdThemeFile.getId(), newThemeFile);

      Assertions.assertEquals("is/updated", updatedThemeFile.getPath());
      Assertions.assertEquals("is updated", updatedThemeFile.getContent());
      Assertions.assertEquals(theme.getId(), updatedThemeFile.getThemeId());
    }
  }

  @Test
  public void deleteExportThemeFileTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();

      //  try {
      fi.metatavu.metaform.api.client.models.ExportThemeFile payload = new fi.metatavu.metaform.api.client.models.ExportThemeFile(
        "to/be/deleted", theme.getId(), "to be deleted", null);

      fi.metatavu.metaform.api.client.models.ExportThemeFile exportThemeFile = testBuilder.metaformSuper.exportfiles().createExportThemeFile(theme.getId(), payload);

      Assertions.assertEquals(1, testBuilder.metaformSuper.exportfiles().listExportThemeFiles(theme.getId()).size());
      Assertions.assertEquals(testBuilder.metaformSuper.exportfiles().
        findExportThemeFile(theme.getId(), exportThemeFile.getId()).toString(), exportThemeFile.toString());

      testBuilder.metaformSuper.exportfiles().deleteExportThemeFile(theme.getId(), exportThemeFile);
      Assertions.assertEquals(0, testBuilder.metaformSuper.exportfiles().listExportThemeFiles(theme.getId()).size());

      testBuilder.metaformSuper.exportfiles().assertFindFailStatus(404, theme.getId(), exportThemeFile.getId());
    }
  }

  @Test
  public void createExportThemePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = new ExportTheme("name", null, null, null);
      testBuilder.test1.exportThemes().assertCreateFailStatus(403, exportTheme);
    }
  }

  @Test
  public void findExportThemePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      testBuilder.test1.exportThemes().assertSearchFailStatus(403, UUID.randomUUID());
    }
  }

  @Test
  public void listExportThemePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      testBuilder.test1.exportThemes().assertListFailStatus(403);
    }
  }

  @Test
  public void updateExportThemePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = new ExportTheme("name", null, null, null);
      testBuilder.test1.exportThemes().assertUpdateFailStatus(403, UUID.randomUUID(), exportTheme);
    }
  }

  @Test
  public void deleteExportThemePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      testBuilder.test1.exportThemes().assertDeleteFailStatus(403, UUID.randomUUID());
    }
  }

  @Test
  public void createExportThemeFilePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      fi.metatavu.metaform.api.client.models.ExportThemeFile payload = new fi.metatavu.metaform.api.client.models.ExportThemeFile("path", exportTheme.getId(), "content", null);

      testBuilder.test1.exportfiles().assertCreateFailStatus(403, payload);
    }
  }

  @Test
  public void findExportThemeFilePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      testBuilder.test1.exportfiles().assertFindExportThemeFileFailStatus(403, exportTheme.getId(), UUID.randomUUID());
    }
  }

  @Test
  public void listExportThemeFilePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      testBuilder.test1.exportfiles().assertListFailStatus(403, exportTheme.getId());
    }
  }

  @Test
  public void updateExportThemeFilePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme exportTheme = testBuilder.metaformSuper.exportThemes().createSimpleExportTheme();
      fi.metatavu.metaform.api.client.models.ExportThemeFile payload = new fi.metatavu.metaform.api.client.models.ExportThemeFile("path", exportTheme.getId(), "content", null);
      testBuilder.test1.exportfiles().assertUpdateFailStatus(403, exportTheme.getId(), UUID.randomUUID(), payload);
    }
  }

  @Test
  public void deleteExportThemeFilePermissionDeniedTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      testBuilder.test1.exportfiles().assertDeleteFailStatus(403, UUID.randomUUID(), UUID.randomUUID());
    }
  }

}
