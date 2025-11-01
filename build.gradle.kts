import org.gradle.kotlin.dsl.implementation

plugins {
	java
    jacoco
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.baxter"
version = "0.0.1-SNAPSHOT"
description = "Authentication Service"

repositories {
	mavenCentral()
}

dependencies {
    // lombok for DI
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.projectreactor:reactor-core:3.5.10")

    // spring logging
    implementation("org.springframework.boot:spring-boot-starter-logging")

    // Reactive database stack
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.asyncer:r2dbc-mysql:1.1.0")

    // JWT support
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Testing
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

jacoco {
    toolVersion = "0.8.13"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check{
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    mainClass.set("io.baxter.authentication.Application")
}

