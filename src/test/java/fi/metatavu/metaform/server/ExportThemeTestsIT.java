package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.ExportTheme;
import fi.metatavu.metaform.client.ExportThemeFile;
import fi.metatavu.metaform.client.ExportThemesApi;

@SuppressWarnings ("squid:S1192")
public class ExportThemeTestsIT extends AbstractIntegrationTest {

  @Test
  public void createExportThemeTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();
      
      ExportTheme parentTheme = dataBuilder.createSimpleExportTheme();

      ExportTheme childPayload = new ExportTheme();
      childPayload.setLocales("locales");
      childPayload.setName("child theme");
      childPayload.setParentId(parentTheme.getId());
      ExportTheme childTheme = dataBuilder.createExportTheme(childPayload);
      assertNotNull(childTheme);
      
      ExportTheme foundChildTheme = adminExportThemesApi.findExportTheme(REALM_1, childTheme.getId());
      
      assertNotNull(foundChildTheme);
      assertEquals(childTheme.toString(), foundChildTheme.toString());
      assertEquals("child theme", foundChildTheme.getName());
      assertEquals("locales", foundChildTheme.getLocales());
      assertEquals(parentTheme.getId(), foundChildTheme.getParentId());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void findExportThemeTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();
      
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      assertNotNull(exportTheme);
      assertNotNull(exportTheme.getName());
      
      ExportTheme foundExportTheme = adminExportThemesApi.findExportTheme(REALM_1, exportTheme.getId());
      
      assertNotNull(foundExportTheme);
      assertEquals(exportTheme.getName(), foundExportTheme.getName());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listExportThemeTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();

      ExportTheme exportTheme1 = dataBuilder.createSimpleExportTheme("theme 1");
      ExportTheme exportTheme2 = dataBuilder.createSimpleExportTheme("theme 2");
      
      List<ExportTheme> exportThemes = adminExportThemesApi.listExportThemes(REALM_1);
      assertEquals(2, exportThemes.size());
      assertEquals(exportTheme1.toString(), exportThemes.get(0).toString());
      assertEquals(exportTheme2.toString(), exportThemes.get(1).toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void updateExportThemeTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();
      
      ExportTheme parentTheme = dataBuilder.createSimpleExportTheme();

      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme("not updated");
      assertNotNull(exportTheme);
      assertEquals("not updated", exportTheme.getName());
      assertNull(exportTheme.getLocales());
      assertNull(exportTheme.getParentId());
      
      exportTheme.setName("updated");
      exportTheme.setLocales("locales");
      exportTheme.setParentId(parentTheme.getId());
      
      ExportTheme updatedTheme = adminExportThemesApi.updateExportTheme(REALM_1, exportTheme.getId(), exportTheme);
      
      assertNotNull(updatedTheme);
      assertEquals("updated", updatedTheme.getName());
      assertEquals("locales", updatedTheme.getLocales());
      assertEquals(parentTheme.getId(), updatedTheme.getParentId());
      
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void deleteExportThemeTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();
      
      ExportTheme payload = new ExportTheme();
      payload.setName("to be deleted");
      ExportTheme exportTheme = adminExportThemesApi.createExportTheme(REALM_1, payload);
      
      assertEquals(1, adminExportThemesApi.listExportThemes(REALM_1).size());
      assertEquals(adminExportThemesApi.findExportTheme(REALM_1, exportTheme.getId()).toString(), exportTheme.toString());
      
      adminExportThemesApi.deleteExportTheme(REALM_1, exportTheme.getId());

      assertEquals(0, adminExportThemesApi.listExportThemes(REALM_1).size());
      
      try {
        adminExportThemesApi.findExportTheme(REALM_1, exportTheme.getId());
        fail("Deleted export theme should not be found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void createExportThemeFileTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      ExportThemeFile exportThemeFile = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file", "file content");

      assertNotNull(exportThemeFile);
      assertEquals("path/to/file", exportThemeFile.getPath());
      assertEquals("file content", exportThemeFile.getContent());
      assertEquals(theme.getId(), exportThemeFile.getThemeId());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void findExportThemeFileTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getAdminExportThemesApi();
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      ExportThemeFile createThemeFile = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file1", "file content 1");  
      assertNotNull(createThemeFile);
      
      ExportThemeFile foundFile = exportThemesApi.findExportThemeFile(REALM_1, theme.getId(), createThemeFile.getId());
      
      assertEquals(createThemeFile.toString(), foundFile.toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listExportThemeFileTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getAdminExportThemesApi();
      
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      
      assertEquals(0, exportThemesApi.listExportThemeFiles(REALM_1, theme.getId()).size());
      
      ExportThemeFile exportThemeFile1 = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file1", "file content 1");
      ExportThemeFile exportThemeFile2 = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file2", "file content 2");
      
      List<ExportThemeFile> themeFiles = exportThemesApi.listExportThemeFiles(REALM_1, theme.getId());
      
      assertEquals(2, themeFiles.size());
      assertEquals(exportThemeFile1.toString(), themeFiles.get(0).toString());
      assertEquals(exportThemeFile2.toString(), themeFiles.get(1).toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void updateExportThemeFileTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      
      ExportThemeFile createdThemeFile = dataBuilder.createSimpleExportThemeFile(theme.getId(), "not/updated", "not updated");
      assertEquals("not/updated", createdThemeFile.getPath());
      assertEquals("not updated", createdThemeFile.getContent());
      assertEquals(theme.getId(), createdThemeFile.getThemeId());

      createdThemeFile.setPath("is/updated");
      createdThemeFile.setContent("is updated");
      
      ExportThemeFile updatedThemeFile = adminExportThemesApi.updateExportThemeFile(REALM_1, theme.getId(), createdThemeFile.getId(), createdThemeFile);
      assertEquals("is/updated", updatedThemeFile.getPath());
      assertEquals("is updated", updatedThemeFile.getContent());
      assertEquals(theme.getId(), updatedThemeFile.getThemeId());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void deleteExportThemeFileTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi adminExportThemesApi = dataBuilder.getAdminExportThemesApi();
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      ExportThemeFile payload = new ExportThemeFile();
      payload.setContent("to be deleted");
      payload.setPath("to/be/dleted");
      payload.setThemeId(theme.getId());
      ExportThemeFile exportThemeFile = adminExportThemesApi.createExportThemeFile(REALM_1, theme.getId(), payload);
      assertEquals(1, adminExportThemesApi.listExportThemeFiles(REALM_1, theme.getId()).size());
      assertEquals(adminExportThemesApi.findExportThemeFile(REALM_1, theme.getId(), exportThemeFile.getId()).toString(), exportThemeFile.toString());
      adminExportThemesApi.deleteExportThemeFile(REALM_1, theme.getId(), exportThemeFile.getId());
      assertEquals(0, adminExportThemesApi.listExportThemeFiles(REALM_1, theme.getId()).size());
      
      try {
        adminExportThemesApi.findExportThemeFile(REALM_1, theme.getId(), exportThemeFile.getId());
        fail("Deleted export theme file should not be found");
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }

    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void createExportThemePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      try {
        ExportTheme exportTheme = new ExportTheme();
        exportTheme.setName("name");
        exportThemesApi.createExportTheme(REALM_1, exportTheme);
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }    
  }
  
  @Test
  public void findExportThemePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      
      try {
        exportThemesApi.findExportTheme(REALM_1, UUID.randomUUID());
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }    
  }
  
  @Test
  public void listExportThemePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      
      try {
        exportThemesApi.listExportThemes(REALM_1);
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }    
  }
  
  @Test
  public void updateExportThemePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      try {
        ExportTheme exportTheme = new ExportTheme();
        exportTheme.setName("name");
        exportThemesApi.updateExportTheme(REALM_1, UUID.randomUUID(), exportTheme);
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }    
  }

  @Test
  public void deleteExportThemePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      
      try {
        exportThemesApi.deleteExportTheme(REALM_1, UUID.randomUUID());
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }    
  }

  @Test
  public void createExportThemeFilePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        ExportThemeFile payload = new ExportThemeFile();
        payload.setContent("content");
        payload.setPath("path");
        payload.setThemeId(exportTheme.getId());
        exportThemesApi.createExportThemeFile(REALM_1, exportTheme.getId(), payload);
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void findExportThemeFilePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        exportThemesApi.findExportThemeFile(REALM_1, exportTheme.getId(), UUID.randomUUID());
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void listExportThemeFilePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        exportThemesApi.listExportThemeFiles(REALM_1, exportTheme.getId());
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void updateExportThemeFilePermissionDeniedTest()  throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        ExportThemeFile payload = new ExportThemeFile();
        payload.setContent("content");
        payload.setPath("path");
        payload.setThemeId(exportTheme.getId());
        exportThemesApi.updateExportThemeFile(REALM_1, exportTheme.getId(), UUID.randomUUID(), payload);
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void deleteExportThemeFilePermissionDeniedTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi exportThemesApi = dataBuilder.getExportThemesApi();
      try {
        exportThemesApi.deleteExportThemeFile(REALM_1, UUID.randomUUID(), UUID.randomUUID());
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
}
