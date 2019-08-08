FROM jboss/wildfly:17.0.1.Final

ARG WILDFLY_VERSION=17.0.1.Final
ARG MAVEN_VERSION=3.6.0
ARG MARIADB_MODULE_VERSION=2.3.0

RUN mkdir /tmp/maven && curl -o /tmp/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz -L https://www.nic.funet.fi/pub/mirrors/apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
RUN tar -xvf /tmp/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /tmp/maven
ENV MAVEN_OPTS=-Dfile.encoding=UTF-8 
RUN /tmp/maven/apache-maven-${MAVEN_VERSION}/bin/mvn package
ADD --chown=jboss target/*.war /opt/jboss/wildfly/standalone/deployments/app.war
ADD --chown=jboss ./docker/entrypoint.sh /opt/docker/entrypoint.sh 
ADD --chown=jboss ./docker/host.cli /opt/docker/host.cli
ADD --chown=jboss ./docker/kubernets-jgroups.cli /opt/docker/kubernets-jgroups.cli
ADD --chown=jboss ./docker/jdbc.cli /opt/docker/jdbc.cli
ADD --chown=jboss ./docker/interfaces.cli /opt/docker/interfaces.cli
ADD --chown=jboss ./docker/env.cli /opt/docker/env.cli
RUN chmod a+x /opt/docker/entrypoint.sh

RUN curl -o /tmp/mariadb-module.zip -L https://static.metatavu.io/wildfly/wildfly-${WILDFLY_VERSION}-mariadb-module-${MARIADB_MODULE_VERSION}.zip

RUN unzip -o /tmp/mariadb-module.zip -d /opt/jboss/wildfly/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/docker/host.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/docker/jdbc.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/docker/kubernets-jgroups.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/docker/interfaces.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/docker/env.cli

RUN rm -fR /tmp/maven

EXPOSE 8080

CMD "/opt/docker/entrypoint.sh"