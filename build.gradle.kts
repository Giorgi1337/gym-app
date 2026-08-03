import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    group = "com.gym"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
            }
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = false
                exceptionFormat = TestExceptionFormat.FULL
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-Xlint:unchecked")
        }
    }
}

val coverageExclusions = mapOf(
    "gym-crm-service" to listOf(
        "com/gym/api/**",
        "com/gym/integration/workload/**",
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
    ),
    "trainer-workload-service" to listOf(
        "com/gym/workload/config/**",
        "com/gym/workload/controller/**",
        "com/gym/workload/dto/**",
        "com/gym/workload/exception/**",
        "com/gym/workload/filter/**",
        "com/gym/workload/TrainerWorkloadApplication.class",
    ),
)

configure(subprojects.filter { it.name in coverageExclusions }) {
    val projectCoverageExclusions = coverageExclusions.getValue(name)

    pluginManager.withPlugin("java") {
        pluginManager.apply("jacoco")

        extensions.configure<JacocoPluginExtension> {
            toolVersion = "0.8.15"
        }

        tasks.withType<Test>().configureEach {
            finalizedBy(tasks.named("jacocoTestReport"))
        }

        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn(tasks.named("test"))
            reports {
                html.required = true
                xml.required = true
                csv.required = false
            }
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) { exclude(projectCoverageExclusions) }
            }))
        }

        tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
            dependsOn(tasks.named("jacocoTestReport"))
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) { exclude(projectCoverageExclusions) }
            }))
            violationRules {
                rule {
                    limit {
                        minimum = BigDecimal.valueOf(0.80)
                    }
                }
            }
        }

        tasks.named("check") {
            dependsOn(tasks.named("jacocoTestCoverageVerification"))
        }
    }
}
