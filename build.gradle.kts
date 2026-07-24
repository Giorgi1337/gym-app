import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.23.0"
    id("net.ltgt.errorprone") version "4.2.0"
    id("jacoco")
}

group = "com"
version = "1.0-SNAPSHOT"
description = "Gym CRM"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val generatedDir = layout.buildDirectory.dir("generated")
val commonsLang3Version = "3.20.0"
val commonsTextVersion = "1.15.0"

repositories {
    mavenCentral()
}

springBoot {
    buildInfo()
}

// Dependencies
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-database-postgresql")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")

    // Utilities
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-scalar:3.0.3")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // NullAway
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    errorprone("com.uber.nullaway:nullaway:0.13.7")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/openapi/gym-crm.yml")
    outputDir.set(generatedDir.get().asFile.absolutePath)

    apiPackage.set("com.gym.api")
    modelPackage.set("com.gym.dto")

    importMappings.set(
        mapOf(
            "ErrorResponse" to "com.gym.exception.ErrorResponse"
        )
    )

    configOptions.set(
        mapOf(
            "useTags"               to "true",
            "interfaceOnly"         to "true",
            "useDeductionForOneOfInterfaces" to "true",
            "openApiNullable"       to "false",
            "useBeanValidation"     to "true",
            "performBeanValidation" to "true",
            "useJakartaEe"          to "true",
            "useJackson3"           to "true",
            "useSpringBoot4"        to "true",
            "useJspecify"           to "true",
            "autoXSpringPaginated"  to "true",
            "generatePageableConstraintValidation" to "true",
            "generateSortValidation" to "true",
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(generatedDir.map { it.dir("src/main/java") })
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

tasks.withType<JavaCompile>().configureEach {
    if (name == "compileJava") {
        options.errorprone {
            disableAllChecks = true
            option("NullAway:OnlyNullMarked", "true")
            error("NullAway")
            option("NullAway:CustomContractAnnotations", "org.springframework.lang.Contract")
            excludedPaths.set(".*/build/generated/.*")
        }
    } else {
        options.errorprone.isEnabled.set(false)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// JaCoCo
jacoco {
    toolVersion = "0.8.15"
}

val jacocoExclusions = listOf(
    "com/gym/api/**",
    "com/gym/metrics/**",
    "com/gym/health/**",
    "com/gym/repository/**",
    "com/gym/security/**",
    "org/openapitools/**",
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
