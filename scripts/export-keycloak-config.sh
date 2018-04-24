#!/bin/bash

. scripts/keycloak-version.sh

$KEYCLOAK/bin/standalone.sh -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Djboss.management.http.port=10190 -Dkeycloak.migration.file=/tmp/keycloak.conf
