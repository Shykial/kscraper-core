import kotlinx.kover.api.CoverageEngine
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val springMockKVersion: String by project
val swaggerVersion: String by project
val restAssuredVersion: String by project
val testContainersVersion: String by project
val javaJwtVersion: String by project
val kotestVersion: String by project
val kotlinLoggingVersion: String by project
val skrapeItVersion: String by project
val mockServerClientVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    id("org.owasp.dependencycheck")
}

group = "com.shykial"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = testContainersVersion

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.auth0:java-jwt:$javaJwtVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.springfox:springfox-boot-starter:$swaggerVersion")
    implementation("it.skrape:skrapeit:$skrapeItVersion") {
        exclude(group = "ch.qos.logback")
    }
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit-vintage-engine")
        exclude(module = "mockito-core")
        exclude(module = "mockito-junit-jupiter")
    }
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:mockserver")
    testImplementation("org.mock-server:mockserver-client-java:$mockServerClientVersion")
    testImplementation("io.rest-assured:spring-web-test-client:$restAssuredVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockKVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs =
            listOf(
                "-Xjsr305=strict",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xcontext-receivers"
            )
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR)
    }
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

val generatedResourcesDir = "$buildDir/generated-resources"

kotlin.sourceSets["main"].kotlin.srcDir("$generatedResourcesDir/src/main/kotlin")

openApiGenerate {
    inputSpec.set("$rootDir/src/main/resources/static/openapi.yaml")
    generatorName.set("kotlin-spring")
    outputDir.set("$buildDir/generated-resources")
    packageName.set("generated.com.shykial.kScraperCore")
    configOptions.putAll(
        mapOf(
            "interfaceOnly" to "true",
            "useTags" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "jackson",
            "reactive" to "true"
        )
    )
}

ktlint {
    filter {
        exclude("**/generated/**")
    }
}

kover {
    coverageEngine.set(CoverageEngine.INTELLIJ)
}

tasks.koverVerify {
    rule {
        bound {
            minValue = 80
        }
    }
    excludes = listOf("generated.*")
}
