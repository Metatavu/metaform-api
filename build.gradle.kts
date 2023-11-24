import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.allopen") version "1.9.10"
    id("io.quarkus")
    id("org.openapi.generator") version "7.1.0"
    id("org.jetbrains.kotlin.kapt") version "1.9.20"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val jaxrsFunctionalTestBuilderVersion: String by project
val wiremockVersion: String by project
val testContainersKeycloakVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-keycloak-admin-client")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-mysql")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-undertow")
    implementation("io.quarkus:quarkus-cache")

    implementation("com.github.slugify:slugify:2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.freemarker:freemarker:2.3.32")
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("fi.metatavu.polyglot:polyglot-xhr:1.0.0") {
        exclude(group="org.graalvm.js", module="js")
    }
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.mailgun:mailgun-java:1.1.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("fi.metatavu.jaxrs.testbuilder:jaxrs-functional-test-builder:$jaxrsFunctionalTestBuilderVersion") {
        exclude(group="com.fasterxml.jackson.core", module="jackson-core")
        exclude(group="com.fasterxml.jackson.core", module="jackson-databind")
        exclude(group="com.fasterxml.jackson.datatype", module="jackson-datatype-jsr310")
    }
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.awaitility:awaitility:3.1.2")
    testImplementation("org.apache.pdfbox:pdfbox:2.0.26")
    testImplementation("org.apache.pdfbox:pdfbox-tools:2.0.26")
    testImplementation("com.github.dasniko:testcontainers-keycloak:$testContainersKeycloakVersion")
    testImplementation("org.wiremock:wiremock:$wiremockVersion")


    // compileOnly("org.hibernate:hibernate-jpamodelgen:6.2.13.Final")
    kapt("org.hibernate:hibernate-jpamodelgen:6.2.13.Final")
}

group = "fi.metatavu.metaform"
version = "2.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets["main"].java {
    srcDir("build/generated/api-spec/src/main/kotlin")
    srcDir("build/generated/keycloak-client/src/main/kotlin")
}

sourceSets["test"].java {
    srcDir("build/generated/api-client/src/main/kotlin")
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("jakarta.persistence.Entity")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}

val generateApiSpec = tasks.register("generateApiSpec",GenerateTask::class){
    setProperty("generatorName", "kotlin-server")
    setProperty("inputSpec",  "$rootDir/metaform-api-spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-spec")
    setProperty("apiPackage", "fi.metatavu.metaform.api.spec")
    setProperty("invokerPackage", "fi.metatavu.metaform.api.spec.invoker")
    setProperty("modelPackage", "fi.metatavu.metaform.api.spec.model")
    setProperty("templateDir", "$rootDir/openapi/api-spec")

    this.configOptions.put("library", "jaxrs-spec")
    this.configOptions.put("dateLibrary", "java8")
    this.configOptions.put("interfaceOnly", "true")
    this.configOptions.put("useCoroutines", "false")
    this.configOptions.put("enumPropertyNaming", "UPPERCASE")
    this.configOptions.put("returnResponse", "true")
    this.configOptions.put("useSwaggerAnnotations", "false")
    this.configOptions.put("useJakartaEe", "true")
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

project.afterEvaluate {

    project.tasks.named("kaptGenerateStubsKotlin") {
        mustRunAfter(generateApiSpec, generateKeycloackClient)
    }

    project.tasks.named("kaptGenerateStubsTestKotlin") {
        mustRunAfter(generateApiClient)
    }

    project.tasks.named("compileQuarkusGeneratedSourcesJava") {
        mustRunAfter(project.tasks.named("compileJava"))
    }
}
