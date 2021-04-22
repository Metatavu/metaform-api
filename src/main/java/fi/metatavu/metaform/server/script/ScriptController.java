package fi.metatavu.metaform.server.script;

import fi.metatavu.metaform.api.spec.model.MetaformScript;

import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


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
    if (scripts != null) {
      scripts.stream().forEach(this::runScript);
    }
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
