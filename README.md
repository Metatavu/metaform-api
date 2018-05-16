# Metaform API

API Service for Metaforms.

## Installing 

### Prerequisites

These instructions assume that system is being installed on machine with Ubuntu 16.04 OS.

### Set environment

Choose installation directory

    export INSTALL_DIR=[Where you want to install the application] 
   
Add desired hostname into hosts file and change it to point to 127.0.0.1. In this example we use dev.metaform.fi

### Install Postgres

    sudo apt-get update
    sudo apt-get install postgresql postgresql-contrib

### Create database and database user

    sudo -u postgres createuser -R -S metaformapi
    sudo -u postgres createdb -Ometaformapi -Ttemplate0 metaformapi
    
### Install Java
  
    sudo apt-get install openjdk-8-jdk openjdk-8-jre

### Install Wildfly

    cd $INSTALL_DIR
    wget "http://download.jboss.org/wildfly/12.0.0.Final/wildfly-12.0.0.Final.zip"
    unzip wildfly-12.0.0.Final.zip
    
### Install Wildfly Postgres Module

    cd $INSTALL_DIR/wildfly-12.0.0.Final
    wget https://www.dropbox.com/s/lt5r6r3grz8gl9s/postgresql-wildfly-module.zip?dl=1 -O postgresql-wildfly-module.zip
    unzip postgresql-wildfly-module.zip
    
### Install Wildfly Keycloak Adapter

    cd $INSTALL_DIR/wildfly-12.0.0.Final
    wget https://downloads.jboss.org/keycloak/4.0.0.Beta2/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-4.0.0.Beta2.zip
    unzip keycloak-wildfly-adapter-dist-4.0.0.Beta2.zip
    sh bin/jboss-cli.sh --file=bin/adapter-elytron-install-offline.cli
    rm keycloak-wildfly-adapter-dist-4.0.0.Beta2.zip

### Configure Wildfly

Start Wildfly in background by running
    
    cd $INSTALL_DIR/wildfly-12.0.0.Final/bin
    sh jboss-cli.sh
    embed-server --server-config=standalone.xml
    /subsystem=datasources/jdbc-driver=postgres:add(driver-module-name="org.postgres",driver-xa-datasource-class-name="org.postgresql.xa.PGXADataSource",driver-datasource-class-name="org.postgresql.ds.PGSimpleDataSource")
    /subsystem=datasources/xa-data-source=metaform:add(jndi-name="java:jboss/datasources/metaform-api", user-name="username", password="password", driver-name="postgres")
    /subsystem=datasources/xa-data-source=metaform/xa-datasource-properties=ServerName:add(value="127.0.0.1")
    /subsystem=datasources/xa-data-source=metaform/xa-datasource-properties=DatabaseName:add(value="metaformapi")
    
    



