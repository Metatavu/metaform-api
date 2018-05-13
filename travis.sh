#!/bin/bash

mvn clean verify jacoco:report coveralls:report -Pitests sonar:sonar -DrepoToken=$COVERALLS_TOKEN