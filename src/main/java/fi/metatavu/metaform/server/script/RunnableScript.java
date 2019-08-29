package fi.metatavu.metaform.server.script;

/**
 * Class that defines single runnable script
 * 
 * @author Antti Leppä
 */
public class RunnableScript {

  private String language;
  private String content;
  private String name;

  /**
   * Constructor
   * 
   * @param language lanuage
   * @param content content
   * @param name name
   */
  public RunnableScript(String language, String content, String name) {
    super();
    this.language = language;
    this.content = content;
    this.name = name;
  }

  public String getLanguage() {
    return language;
  }

  public String getContent() {
    return content;
  }

  public String getName() {
    return name;
  }

}
