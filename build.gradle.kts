import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.diffplug.gradle.spotless.SpotlessExtension

val projectGroup: String by project
val projectVersion: String by project

plugins {
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.craigmiller160.gradle.defaults") version "1.1.0"
    id("com.diffplug.spotless") version "6.17.0"
    `maven-publish`
}

dependencyManagement {
    imports {
        mavenBom("org.keycloak.bom:keycloak-adapter-bom:20.0.2")
    }
}

group = projectGroup
version = projectVersion
java.sourceCompatibility = JavaVersion.VERSION_20

dependencies {
    val springDocVersion: String by project

    testImplementation("io.craigmiller160:testcontainers-common:1.2.0-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.keycloak:keycloak-admin-client")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springDocVersion")
    implementation("org.springdoc:springdoc-openapi-ui:$springDocVersion")
    implementation("com.querydsl:querydsl-jpa:5.0.0")
    implementation("com.opencsv:opencsv:5.6")
    testImplementation("com.github.javafaker:javafaker:1.0.2") {
        exclude("org.yaml", "snakeyaml")
    }
    implementation("org.keycloak:keycloak-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.kotest.extensions:kotest-assertions-arrow-jvm:1.2.5")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}