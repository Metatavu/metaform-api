#!/bin/bash

mvn clean verify coveralls:report org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -DrepoToken=$COVERALLS_TOKEN --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn