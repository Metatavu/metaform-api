# Metaform API

API Service for Metaforms.

## Installing 

### Prerequisites

These instructions assume that system is being installed on machine with Ubuntu 16.04 OS and that you have DNS address pointing to your server.

### Set environment variables

   export INSTALL_DIR=[Where you want to install the application] 

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
    
### Install Wildfly Keycloak Adapter

    cd $INSTALL_DIR/wildfly-12.0.0.Final
    wget https://downloads.jboss.org/keycloak/4.0.0.Beta2/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-4.0.0.Beta2.zip
    unzip keycloak-wildfly-adapter-dist-4.0.0.Beta2.zip
    sh bin/jboss-cli.sh --file=bin/adapter-elytron-install-offline.cli
    rm keycloak-wildfly-adapter-dist-4.0.0.Beta2.zip

### Configure Wildfly

Start Wildfly in another console by running
    
    cd $INSTALL_DIR/wildfly-12.0.0.Final/bin
    sh standalone.sh
   
