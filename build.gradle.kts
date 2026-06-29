plugins {
    id("java")
    id("jacoco")
    id("application")
}

group = "com"
version = "1.0-SNAPSHOT"
description = "Gym CRM"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

// Dependency Versions
val springVersion = "7.0.7"
val jacksonVersion = "3.2.0"
val lombokVersion = "1.18.46"
val logbackVersion = "1.5.34"
val junitVersion = "6.0.0"
val commonsLang3Version = "3.20.0"
val commonsTextVersion = "1.15.0"
val jakartaAnnotVersion = "3.0.0"
val jacocoVersion = "0.8.14"
val mockitoVersion = "5.23.0"
val assertjCore = "3.27.7"
val hibernateVersion = "7.4.1.Final"
val flywayVersion = "12.8.1"
val tomcatVersion = "11.0.22"
val springDocVersion = "3.0.3"

repositories {
    mavenCentral()
}

// Dependencies
dependencies {

    // // Embedded Tomcat
    implementation("org.apache.tomcat.embed:tomcat-embed-core:$tomcatVersion")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:$tomcatVersion")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

    // Spring
    implementation("org.springframework:spring-webmvc:$springVersion")
    implementation("org.springframework:spring-web:$springVersion")
    implementation("org.springframework:spring-core:$springVersion")
    implementation("org.springframework:spring-context:$springVersion")
    implementation("org.springframework:spring-orm:$springVersion")
    implementation("org.springframework:spring-tx:$springVersion")
    implementation("org.springframework:spring-aop:$springVersion")

    // Hibernate
    implementation("org.hibernate.orm:hibernate-core:$hibernateVersion")
    implementation("org.hibernate.orm:hibernate-hikaricp:$hibernateVersion")
    implementation("org.hibernate.validator:hibernate-validator:9.1.0.Final")
    annotationProcessor("org.hibernate.validator:hibernate-validator-annotation-processor:9.1.0.Final")

    // AOP
    implementation("org.aspectj:aspectjweaver:1.9.25.1")

    // Flyway
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // Expression Language (required by Hibernate Validator)
    implementation("org.glassfish.expressly:expressly:6.0.0")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.11")

    // Jakarta
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotVersion")

    // Jackson
    implementation("tools.jackson.core:jackson-core:$jacksonVersion")
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")
    implementation("tools.jackson.datatype:jackson-datatype-json-org:$jacksonVersion")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Utilities
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Testing
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation("org.assertj:assertj-core:$assertjCore")
    testImplementation("org.hamcrest:hamcrest:3.0")
    implementation("com.jayway.jsonpath:json-path:3.0.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Compile
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// Test
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// JaCoCo
jacoco {
    toolVersion = jacocoVersion
}

val jacocoExclusions = listOf(
    "com/gym/dao/**",
    "com/gym/config/**",
    "com/gym/dto/**",
    "com/gym/mapper/**",
    "com/gym/exception/**",
    "com/gym/filter/**",
    "com/gym/GymApplication.class",
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.80)
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

application {
    mainClass.set("com.gym.GymApplication")
    applicationDefaultJvmArgs = listOf("--enable-preview")
}