import kotlinx.kover.api.CoverageEngine
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val coroutinesVersion = "1.6.0"
val restAssuredVersion = "4.5.1"
val testContainersVersion = "1.16.3"

plugins {
    id("org.springframework.boot") version "2.6.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.openapi.generator") version "5.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "com.shykial"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = testContainersVersion

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.auth0:java-jwt:3.19.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("it.skrape:skrapeit:1.2.1")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("io.springfox:springfox-boot-starter:3.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
        exclude("org.mockito", "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:mongodb:$testContainersVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.rest-assured:xml-path:$restAssuredVersion")
    testImplementation("io.rest-assured:spring-web-test-client:$restAssuredVersion")
    testImplementation("io.rest-assured:kotlin-extensions:$restAssuredVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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
    packageName.set("generated.com.shykial.kScrapperCore")
    configOptions.putAll(
        mapOf(
            "interfaceOnly" to "true",
            "useTags" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "delegatePattern" to "true",
            "reactive" to "true",
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
}
