import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
    id("io.quarkus")
    id("org.openapi.generator") version "6.3.0"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"
}

configurations {
    all() {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val jaxrsFunctionalTestBuilderVersion: String by project
val testContainersKeycloakVersion: String by project
val moshiVersion: String by project
val wiremockVersion: String by project
val freemarkerVersion: String by project
val quarkusRegisterReflectionVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-keycloak-admin-client")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-mysql")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-undertow")
    
    implementation("io.quarkiverse.poi:quarkus-poi:1.0.2")

    implementation("org.jboss.logging:commons-logging-jboss-logging")
    implementation("org.jboss.logmanager:log4j-jboss-logmanager")

    implementation("org.jboss.spec.javax.security.jacc:jboss-jacc-api_1.5_spec:2.0.0.Final")
    implementation("org.freemarker:freemarker:$freemarkerVersion")

    implementation("net.sargue:mailgun:1.9.2")
    implementation("fi.metatavu.polyglot:polyglot-xhr:1.0.0")
    implementation("com.github.slugify:slugify:2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.squareup.okhttp3:okhttp")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("fi.metatavu.jaxrs.testbuilder:jaxrs-functional-test-builder:$jaxrsFunctionalTestBuilderVersion")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.awaitility:awaitility:3.1.2")
    testImplementation("org.apache.pdfbox:pdfbox:2.0.26")
    testImplementation("org.apache.pdfbox:pdfbox-tools:2.0.26")
    testImplementation("com.github.dasniko:testcontainers-keycloak:$testContainersKeycloakVersion")
    testImplementation("com.github.tomakehurst:wiremock-jre8:$wiremockVersion")

    kapt("org.hibernate:hibernate-jpamodelgen:5.5.7.Final")
}

group = "fi.metatavu.metaform-api"
version = "2.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets["main"].java {
    srcDir("build/generated/api-spec/src/main/kotlin")
    srcDir("build/generated/keycloak-client/src/main/kotlin")
}

sourceSets["test"].java {
    srcDir("build/generated/api-client/src/main/kotlin")
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}

val generateApiSpec = tasks.register("generateApiSpec",GenerateTask::class){
    setProperty("generatorName", "kotlin-server")
    setProperty("inputSpec",  "$rootDir/metaform-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-spec")
    setProperty("apiPackage", "fi.metatavu.metaform.api.spec")
    setProperty("invokerPackage", "fi.metatavu.metaform.api.spec.invoker")
    setProperty("modelPackage", "fi.metatavu.metaform.api.spec.model")

    this.configOptions.put("library", "jaxrs-spec")
    this.configOptions.put("dateLibrary", "java8")
    this.configOptions.put("interfaceOnly", "true")
    this.configOptions.put("useCoroutines", "false")
    this.configOptions.put("enumPropertyNaming", "UPPERCASE")
    this.configOptions.put("returnResponse", "true")
    this.configOptions.put("useSwaggerAnnotations", "false")
    this.configOptions.put("additionalModelTypeAnnotations", "@io.quarkus.runtime.annotations.RegisterForReflection")
}

val generateApiClient = tasks.register("generateApiClient",GenerateTask::class){
    setProperty("generatorName", "kotlin")
    setProperty("library", "jvm-okhttp3")
    setProperty("inputSpec",  "$rootDir/metaform-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-client")
    setProperty("packageName", "fi.metatavu.metaform.api.client")
    this.configOptions.put("dateLibrary", "string")
    this.configOptions.put("collectionType", "array")
    this.configOptions.put("enumPropertyNaming", "UPPERCASE")
    this.configOptions.put("serializationLibrary", "jackson")
}

val generateKeycloackClient = tasks.register("generateKeycloackClient",GenerateTask::class){
    setProperty("generatorName", "kotlin")
    setProperty("library", "jvm-okhttp3")
    setProperty("inputSpec",  "$rootDir/keycloak-openapi/OpenApiDefinitions/keycloak-19.0.0.yml")
    setProperty("outputDir", "$buildDir/generated/keycloak-client")
    setProperty("packageName", "fi.metatavu.metaform.keycloak.client")
    this.configOptions.put("dateLibrary", "java8")
    this.configOptions.put("serializationLibrary", "jackson")
    this.configOptions.put("additionalModelTypeAnnotations", "@io.quarkus.runtime.annotations.RegisterForReflection")
}

tasks.named("compileKotlin") {
    dependsOn(generateApiSpec, generateKeycloackClient)
}

tasks.named("compileTestKotlin") {
    dependsOn(generateApiClient)
}

tasks.named("clean") {
    this.doFirst {
        file("$rootDir/src/gen").deleteRecursively()
    }
}
