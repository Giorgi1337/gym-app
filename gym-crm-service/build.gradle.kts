import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator") version "7.23.0"
    id("net.ltgt.errorprone") version "4.2.0"
}

description = "gym-crm-service"

val generatedDir = layout.buildDirectory.dir("generated")
val commonsLang3Version = "3.20.0"
val commonsTextVersion = "1.15.0"
val springCloudVersion = "2025.1.2"


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

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

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

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set(layout.projectDirectory.file("src/main/resources/openapi/gym-crm.yml").asFile.absolutePath)
    outputDir.set(generatedDir.get().asFile.absolutePath)

    apiPackage.set("com.gym.api")
    modelPackage.set("com.gym.dto")
    importMappings.set(mapOf("ErrorResponse" to "com.gym.exception.ErrorResponse"))
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

