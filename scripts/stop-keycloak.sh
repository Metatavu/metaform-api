#!/bin/bash

. scripts/keycloak-version.sh

${KEYCLOAK}/bin/jboss-cli.sh --controller=localhost:10190 --connect command=:shutdown
