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
    sudo -u postgres psql 
    alter user metaformapi with password 'password';    
    
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
    
### Install Keycloak

     cd $INSTALL_DIR
     wget https://downloads.jboss.org/keycloak/3.4.3.Final/keycloak-3.4.3.Final.zip
     unzip keycloak-3.4.3.Final.zip
     
### Start Keycloak

In order to use the API, Keycloak must be running, so starting it in another console would be a good idea.

     cd $INSTALL_DIR/keycloak-3.4.3.Final/bin/
     sh standalone.sh -Djboss.socket.binding.port-offset=200
     
### Setup Keycloak realm

- Navigate into *http://localhost:8280/auth*. 
- Create admin user
- Login with newly created user
- Create new realm
- Create new client with following settings:
  - Client Protocol: openid-connect
  - Access Type: bearer-only
- Click installation tab
- Select Keycloak OIDC JSON format
- Download the configuration and save it into $INSTALL_FOLDER/realms -folder and name it as realm.json (where realm is your realm's name)
  
### Configure Wildfly

Start Wildfly in background by running
    
    cd $INSTALL_DIR/wildfly-12.0.0.Final/bin
    sh jboss-cli.sh
    embed-server --server-config=standalone.xml
    /subsystem=datasources/jdbc-driver=postgres:add(driver-module-name="org.postgres",driver-xa-datasource-class-name="org.postgresql.xa.PGXADataSource",driver-datasource-class-name="org.postgresql.ds.PGSimpleDataSource")
    /subsystem=datasources/xa-data-source=metaform:add(jndi-name="java:jboss/datasources/metaform-api", user-name="username", password="password", driver-name="postgres")
    /subsystem=datasources/xa-data-source=metaform/xa-datasource-properties=ServerName:add(value="localhost")
    /subsystem=datasources/xa-data-source=metaform/xa-datasource-properties=DatabaseName:add(value="metaformapi")
    /subsystem=undertow/server=default-server/host=metaform-api:add(default-web-module="metaform-api.war",alias=["dev.metaform.fi"])
    exit
    
### Compile and deploy Metaform API

Compile application

    sudo apt install git maven
    cd $INSTALL_DIR
    git clone https://github.com/Metatavu/metaform-api.git
    mvn clean package
    
Deploy by copying war-archive into the Wildfly deployments -folder:

    cp $INSTALL_DIR/metaform-api/target/*.war $INSTALL_DIR/wildfly-12.0.0.Final/standalone/deployments/
    
And start the Wildfly by running

    $INSTALL_DIR/wildfly-12.0.0.Final/bin/standalone.sh
    
And you're done, API should respond with "Forbidden" from port 8080 in your defined host e.g. http://dev.metaform.fi:8080/
