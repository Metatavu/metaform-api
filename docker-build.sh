mvn clean package
VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')
docker build -t metaform-api .
docker tag $(docker images -q metaform-api) metatavu/metaform-api:$VERSION
docker push metatavu/metaform-api
