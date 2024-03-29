pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val dependencyManagementVersion: String by settings
    val openapiGeneratorVersion: String by settings
    val ktlintVersion: String by settings
    val koverVersion: String by settings
    val owaspPluginVersion: String by settings
    val avroPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version dependencyManagementVersion
        id("org.openapi.generator") version openapiGeneratorVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
        id("org.owasp.dependencycheck") version owaspPluginVersion
        id("com.github.davidmc24.gradle.plugin.avro") version avroPluginVersion
    }
}

rootProject.name = "kScraper-core"
