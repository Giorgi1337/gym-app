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
val springVersion        = "7.0.7"
val jacksonVersion       = "3.1.4"
val lombokVersion        = "1.18.46"
val logbackVersion       = "1.5.34"
val junitVersion         = "6.0.0"
val commonsLang3Version  = "3.20.0"
val commonsTextVersion   = "1.15.0"
val jakartaAnnotVersion  = "3.0.0"
val jacocoVersion        = "0.8.14"
val mockitoVersion       = "5.23.0"
val assertjCore         = "3.27.7"

repositories {
    mavenCentral()
}

dependencies {

    // Spring
    implementation("org.springframework:spring-core:$springVersion")
    implementation("org.springframework:spring-context:$springVersion")

    // Jakarta
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotVersion")

    // Jackson
    implementation("tools.jackson.core:jackson-core:$jacksonVersion")
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

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
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:$assertjCore")
}

// Compile
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
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

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.80)
            }
        }
    }
}

application {
    mainClass.set("com.gym.GymApplication")
    applicationDefaultJvmArgs = listOf("--enable-preview")
}