package fi.metatavu.metaform.server.liquibase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * Liquibase producer
 * 
 * @author Antti Leppä
 */
@Dependent
public class LiquibaseProducer {
  
  @Resource (lookup = "java:jboss/datasources/metaform-api")
  private DataSource dataSource;
  
  /**
   * Creates Liquibase config
   * 
   * @return Liquibase config
   */
  @Produces
  @LiquibaseType
  public CDILiquibaseConfig createConfig() {
    List<String> contextList = new ArrayList<>();
    
    if ("TEST".equals(System.getProperty("runmode"))) {
      contextList.add("test");
    } else {
      contextList.add("production");
    }
    
    String contexts = StringUtils.join(contextList, ',');
    CDILiquibaseConfig config = new CDILiquibaseConfig();
    config.setChangeLog("fi/metatavu/metaform/server/liquibase/changelog.xml");
    config.setContexts(contexts);

    return config;
  }
  
  /**
   * Creates Liquibase data source
   * 
   * @return Liquibase data source
   * @throws SQLException
   */
  @Produces
  @LiquibaseType
  public DataSource createDataSource() throws SQLException {
    return dataSource;
  }
  
  /**
   * Creates resource accessor for Liquibase
   * 
   * @return resource accessor
   */
  @Produces
  @LiquibaseType
  public ResourceAccessor create() {
    return new ClassLoaderResourceAccessor(getClass().getClassLoader());
  }

}
