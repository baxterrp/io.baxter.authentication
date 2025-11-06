import org.gradle.kotlin.dsl.implementation

plugins {
	java
    jacoco
    id("org.sonarqube") version "5.1.0.4882"
    id("co.uzzu.dotenv.gradle") version "2.0.0"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

group = "io.baxter"
version = "0.0.1-SNAPSHOT"
description = "Authentication Service"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
    // logback
    implementation("ch.qos.logback:logback-core:1.5.19")
    implementation("ch.qos.logback:logback-classic:1.5.19")

    // lombok for di
    compileOnly("org.projectlombok:lombok:1.18.40")
    annotationProcessor("org.projectlombok:lombok:1.18.40")

    // spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // spring swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0")

    // spring logging
    implementation("org.springframework.boot:spring-boot-starter-logging")

    // reactive database stack
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.asyncer:r2dbc-mysql:1.1.0")

    // jwt support
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // unit testing
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.all {
    resolutionStrategy.force("ch.qos.logback:logback-core:1.5.19")
    resolutionStrategy.force("ch.qos.logback:logback-classic:1.5.19")
}

openApi {
    outputDir = layout.buildDirectory.dir("openapi")
    outputFileName = "openapi.json"
    apiDocsUrl = "/v3/api-docs"
    waitTimeInSeconds = 10

    val baseUrl = System.getenv("API_URL") ?: "http://localhost:9000"
    apiDocsUrl = "$baseUrl/v3/api-docs"

    customBootRun {}
}

jacoco {
    toolVersion = "0.8.13"
}

sonarqube {
    properties {
        property("sonar.projectKey", "baxterrp_io-baxter-authentication")
        property("sonar.organization", "baxterrp")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.junit.reportPaths", "build/test-results/test")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check{
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    mainClass.set("io.baxter.authentication.api.Application")
}

