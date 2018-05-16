# Metaform API

API Service for Metaforms.

## Installing 

### Prerequisites

These instructions assume that system is being installed on machine with Ubuntu 16.04 OS and that you have DNS address pointing to your server.

### Install Postgres

    sudo apt-get update
    sudo apt-get install postgresql postgresql-contrib

### Create database and database user

    sudo -u postgres createuser -R -S metaformapi
    sudo -u postgres createdb -Ometaformapi -Ttemplate0 metaformapi
