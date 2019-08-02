#!/bin/bash

mvn clean verify org.jacoco:jacoco-maven-plugin:prepare-agent coveralls:report sonar:sonar -DrepoToken=$COVERALLS_TOKEN --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn