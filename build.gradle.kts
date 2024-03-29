import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
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
val jasyptVersion: String by project
val springDocVersion: String by project
val apacheAvroVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    id("org.owasp.dependencycheck")
    id("com.github.davidmc24.gradle.plugin.avro")
}

group = "com.shykial"
version = "1.0.0"
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
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.auth0:java-jwt:$javaJwtVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$springDocVersion")

    implementation("it.skrape:skrapeit:$skrapeItVersion") {
        exclude(group = "ch.qos.logback")
    }
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:$jasyptVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("org.apache.avro:avro:$apacheAvroVersion")

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
    testImplementation("org.testcontainers:rabbitmq")
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
    dependsOn(tasks.openApiGenerate, "generate-avro")
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xcontext-receivers"
        )
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.STANDARD_OUT)
    }
}

val generatedResourcesDir = "$buildDir/generated-resources"
val staticResourcesDir = "$rootDir/src/main/resources/static"

kotlin.sourceSets["main"].kotlin.srcDir("$generatedResourcesDir/src/main/kotlin")
java.sourceSets["main"].java.srcDir("$generatedResourcesDir/src/main/java")

openApiGenerate {
    inputSpec.set("$staticResourcesDir/openapi/openapi.yaml")
    generatorName.set("kotlin-spring")
    outputDir.set(generatedResourcesDir)
    packageName.set("generated.com.shykial.kScraperCore")
    templateDir.set("$staticResourcesDir/openapi/kotlin-spring-custom-template")
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

tasks.register<GenerateAvroJavaTask>("generate-avro") {
    source("$staticResourcesDir/avro")
    setOutputDir(File("$generatedResourcesDir/src/main/java"))
}

avro {
    isCreateSetters.set(false)
    isCreateOptionalGetters.set(false)
    isGettersReturnOptional.set(false)
    fieldVisibility.set("PRIVATE")
    outputCharacterEncoding.set("UTF-8")
    stringType.set("String")
    templateDirectory.set(null as String?)
    isEnableDecimalLogicalType.set(true)
}

ktlint {
    filter {
        exclude("**/generated/**")
    }
}

kover {
    verify {
        rule {
            bound {
                minValue = 80
            }
        }
    }
    filters {
        classes {
            excludes += listOf("generated.*")
        }
    }
}
