package fi.metatavu.metaform.server.script;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;

import fi.metatavu.polyglot.xhr.XMLHttpRequest;

/**
 * Script processor 
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ScriptProcessor {
  
  private static final Set<String> RESERVED_PARAMS = new HashSet<>(Arrays.asList("name", "version", "module", "function", "target"));
  
  @Inject
  private Logger logger;

  @Inject
  private FormScriptBinding formScriptBinding;
  
  /**
   * Processes a script
   * 
   * @param script script
   * @param params parameters
   * @return processed output
   */
  public String processScript(RunnableScript script, Map<String, String> params) {
    try (Context scriptingContext = createContext(script)) {
      Map<String, String> scriptArgs = new HashMap<>();
      
      params.keySet().stream().forEach(param -> {
        if (!RESERVED_PARAMS.contains(param)) {
          scriptArgs.put(param, params.get(param));
        }
      });
      
      Value bindings = scriptingContext.getBindings(script.getLanguage());

      bindings.putMember("XMLHttpRequest", XMLHttpRequest.class);
      bindings.putMember("form", formScriptBinding);
      bindings.putMember("args", scriptArgs);
      
      Source source = Source
        .newBuilder(script.getLanguage(), script.getContent(), script.getName())
        .build();
  
      Value returnValue = scriptingContext.eval(source);
      
      if (returnValue.isString()) {      
        return returnValue.asString();
      }

    } catch (Exception e) {
      logger.error("Error running script", e);
    }
    
    return "";
  }

  private Context createContext(RunnableScript script) {
    return Context.newBuilder(script.getLanguage())
      .allowHostAccess(HostAccess.ALL)
      .allowAllAccess(true)
      .allowCreateThread(true)
      .allowHostClassLoading(true)
      .allowIO(true)
      .allowNativeAccess(true)
      .allowCreateProcess(true)
      .build();
  }
  
  
}
