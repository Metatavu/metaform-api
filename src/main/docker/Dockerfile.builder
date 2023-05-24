# docker build -f src/main/docker/Dockerfile.builder -t quarkus/metaform-api-builder .
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3-java17
USER root
RUN gu install js