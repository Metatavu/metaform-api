package fi.metatavu.metaform.test.functional;

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
import fi.metatavu.metaform.client.model.ExportTheme;
import fi.metatavu.metaform.client.model.ExportThemeFile;
import fi.metatavu.metaform.client.api.ExportThemeFilesApi;
import fi.metatavu.metaform.client.api.ExportThemesApi;

@SuppressWarnings ("squid:S1192")
public class ExportThemeTestsIT extends AbstractIntegrationTest {

  @Test
  public void createExportThemeTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemesApi superExportThemesApi = dataBuilder.getSuperExportThemesApi();
      
      ExportTheme parentTheme = dataBuilder.createSimpleExportTheme();

      ExportTheme childPayload = new ExportTheme();
      childPayload.setLocales("locales");
      childPayload.setName("child theme");
      childPayload.setParentId(parentTheme.getId());
      ExportTheme childTheme = dataBuilder.createExportTheme(childPayload);
      assertNotNull(childTheme);
      
      ExportTheme foundChildTheme = superExportThemesApi.findExportTheme(childTheme.getId());
      
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
      ExportThemesApi superExportThemesApi = dataBuilder.getSuperExportThemesApi();
      
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      assertNotNull(exportTheme);
      assertNotNull(exportTheme.getName());
      
      ExportTheme foundExportTheme = superExportThemesApi.findExportTheme(exportTheme.getId());
      
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
      ExportThemesApi superExportThemesApi = dataBuilder.getSuperExportThemesApi();

      ExportTheme exportTheme1 = dataBuilder.createSimpleExportTheme("theme 1");
      ExportTheme exportTheme2 = dataBuilder.createSimpleExportTheme("theme 2");
      
      List<ExportTheme> exportThemes = superExportThemesApi.listExportThemes();
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
      ExportThemesApi superExportThemesApi = dataBuilder.getSuperExportThemesApi();
      
      ExportTheme parentTheme = dataBuilder.createSimpleExportTheme();

      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme("not updated");
      assertNotNull(exportTheme);
      assertEquals("not updated", exportTheme.getName());
      assertNull(exportTheme.getLocales());
      assertNull(exportTheme.getParentId());
      
      exportTheme.setName("updated");
      exportTheme.setLocales("locales");
      exportTheme.setParentId(parentTheme.getId());
      
      ExportTheme updatedTheme = superExportThemesApi.updateExportTheme(exportTheme.getId(), exportTheme);
      
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
      ExportThemesApi superExportThemesApi = dataBuilder.getSuperExportThemesApi();
      
      ExportTheme payload = new ExportTheme();
      payload.setName("to be deleted");
      ExportTheme exportTheme = superExportThemesApi.createExportTheme(payload);
      
      assertEquals(1, superExportThemesApi.listExportThemes().size());
      assertEquals(superExportThemesApi.findExportTheme(exportTheme.getId()).toString(), exportTheme.toString());
      
      superExportThemesApi.deleteExportTheme(exportTheme.getId());

      assertEquals(0, superExportThemesApi.listExportThemes().size());
      
      try {
        superExportThemesApi.findExportTheme(exportTheme.getId());
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
      ExportThemeFilesApi superExportThemeFilesApi = dataBuilder.getSuperExportThemeFilesApi();
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      ExportThemeFile createThemeFile = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file1", "file content 1");  
      assertNotNull(createThemeFile);
      
      ExportThemeFile foundFile = superExportThemeFilesApi.findExportThemeFile(theme.getId(), createThemeFile.getId());
      
      assertEquals(createThemeFile.toString(), foundFile.toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listExportThemeFileTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      ExportThemeFilesApi superExportThemeFilesApi = dataBuilder.getSuperExportThemeFilesApi();
      
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      
      assertEquals(0, superExportThemeFilesApi.listExportThemeFiles(theme.getId()).size());
      
      ExportThemeFile exportThemeFile1 = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file1", "file content 1");
      ExportThemeFile exportThemeFile2 = dataBuilder.createSimpleExportThemeFile(theme.getId(), "path/to/file2", "file content 2");
      
      List<ExportThemeFile> themeFiles = superExportThemeFilesApi.listExportThemeFiles(theme.getId());
      
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
      ExportThemeFilesApi superExportThemeFilesApi = dataBuilder.getSuperExportThemeFilesApi();
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      
      ExportThemeFile createdThemeFile = dataBuilder.createSimpleExportThemeFile(theme.getId(), "not/updated", "not updated");
      assertEquals("not/updated", createdThemeFile.getPath());
      assertEquals("not updated", createdThemeFile.getContent());
      assertEquals(theme.getId(), createdThemeFile.getThemeId());

      createdThemeFile.setPath("is/updated");
      createdThemeFile.setContent("is updated");
      
      ExportThemeFile updatedThemeFile = superExportThemeFilesApi.updateExportThemeFile(theme.getId(), createdThemeFile.getId(), createdThemeFile);
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
      ExportThemeFilesApi superExportThemeFilesApi = dataBuilder.getSuperExportThemeFilesApi();
      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      ExportThemeFile payload = new ExportThemeFile();
      payload.setContent("to be deleted");
      payload.setPath("to/be/dleted");
      payload.setThemeId(theme.getId());
      ExportThemeFile exportThemeFile = superExportThemeFilesApi.createExportThemeFile(theme.getId(), payload);
      assertEquals(1, superExportThemeFilesApi.listExportThemeFiles(theme.getId()).size());
      assertEquals(superExportThemeFilesApi.findExportThemeFile(theme.getId(), exportThemeFile.getId()).toString(), exportThemeFile.toString());
      superExportThemeFilesApi.deleteExportThemeFile(theme.getId(), exportThemeFile.getId());
      assertEquals(0, superExportThemeFilesApi.listExportThemeFiles(theme.getId()).size());
      
      try {
        superExportThemeFilesApi.findExportThemeFile(theme.getId(), exportThemeFile.getId());
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
        exportThemesApi.createExportTheme(exportTheme);
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
        exportThemesApi.findExportTheme(UUID.randomUUID());
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
        exportThemesApi.listExportThemes();
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
        exportThemesApi.updateExportTheme(UUID.randomUUID(), exportTheme);
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
        exportThemesApi.deleteExportTheme(UUID.randomUUID());
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
      ExportThemeFilesApi exportThemeFilesApi = dataBuilder.getExportThemeFilesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        ExportThemeFile payload = new ExportThemeFile();
        payload.setContent("content");
        payload.setPath("path");
        payload.setThemeId(exportTheme.getId());
        exportThemeFilesApi.createExportThemeFile(exportTheme.getId(), payload);
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
      ExportThemeFilesApi exportThemeFilesApi = dataBuilder.getExportThemeFilesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        exportThemeFilesApi.findExportThemeFile(exportTheme.getId(), UUID.randomUUID());
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
      ExportThemeFilesApi exportThemeFilesApi = dataBuilder.getExportThemeFilesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        exportThemeFilesApi.listExportThemeFiles(exportTheme.getId());
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
      ExportThemeFilesApi exportThemeFilesApi = dataBuilder.getExportThemeFilesApi();
      ExportTheme exportTheme = dataBuilder.createSimpleExportTheme();
      
      try {
        ExportThemeFile payload = new ExportThemeFile();
        payload.setContent("content");
        payload.setPath("path");
        payload.setThemeId(exportTheme.getId());
        exportThemeFilesApi.updateExportThemeFile(exportTheme.getId(), UUID.randomUUID(), payload);
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
      ExportThemeFilesApi exportThemeFilesApi = dataBuilder.getExportThemeFilesApi();
      try {
        exportThemeFilesApi.deleteExportThemeFile(UUID.randomUUID(), UUID.randomUUID());
        fail("Should not be permitted");
      } catch (FeignException e) {
        assertEquals(403, e.status());
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
}
