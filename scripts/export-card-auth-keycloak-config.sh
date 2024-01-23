#/bin/sh

REALM=test-1
CONTAINER_NAME=card-auth-kc

docker exec -e JDBC_PARAMS='?useSSL=false'  -ti $CONTAINER_NAME  /opt/keycloak/bin/kc.sh export --realm $REALM --file /tmp/my_realm.json -Djboss.socket.binding.port-offset=102 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.realmName=$REALM -Dkeycloak.migration.usersExportStrategy=REALM_FILE -Dkeycloak.migration.file=/tmp/my_realm.json
docker cp $CONTAINER_NAME:/tmp/my_realm.json /tmp/my_realm.json
cp /tmp/my_realm.json src/test/resources/exported-card-auth-kc.json

