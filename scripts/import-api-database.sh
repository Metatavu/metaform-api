#!/bin/bash

echo "Starting docker container for MySQL..."
docker-compose up -d mysql

sleep 5

CONTAINER_ID=metaform-api-mysql-1
echo "MySQL container started with ID: $CONTAINER_ID"

docker cp ../db_dumps/api.sql $CONTAINER_ID:/tmp/api.sql
echo "Copied database dump for API"

echo "Creating databases with dump data..."
docker exec $CONTAINER_ID  mysql -uroot -proot -e 'DROP DATABASE IF EXISTS `api`; CREATE DATABASE `api` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */; USE api; source /tmp/api.sql; commit;'

docker-compose down