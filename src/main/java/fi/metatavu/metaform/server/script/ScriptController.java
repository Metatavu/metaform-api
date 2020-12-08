package fi.metatavu.metaform.server.script;

import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.rest.model.MetaformScript;

/**
 * Controller for Metaform scripts
 */
@ApplicationScoped
public class ScriptController {

  @Inject
  private ScriptProcessor scriptProcessor;
  
  /**
   * Runs given scripts
   * 
   * @param scripts scripts
   */
  public void runScripts(List<MetaformScript> scripts) {
    scripts.stream().forEach(this::runScript);
  }
  
  /**
   * Runs given script
   * 
   * @param script
   */
  private void runScript(MetaformScript script) {
    if (script != null) {
      scriptProcessor.processScript(new RunnableScript(script.getLanguage(), script.getContent(), script.getName()), new HashMap<>());
    }
  }

}
