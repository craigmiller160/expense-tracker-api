import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.diffplug.gradle.spotless.SpotlessExtension
import org.springframework.boot.gradle.tasks.run.BootRun

val projectGroup: String by project
val projectVersion: String by project

plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")
    id("io.craigmiller160.gradle.defaults") version "1.2.2"
    id("com.diffplug.spotless") version "6.17.0"
    `maven-publish`
}

dependencyManagement {
    imports {
        mavenBom("org.springdoc:springdoc-openapi:2.0.3")
    }
}

group = projectGroup
version = projectVersion
java.sourceCompatibility = JavaVersion.VERSION_19

dependencies {
    val queryDslVersion: String by project

    implementation("io.craigmiller160:spring-keycloak-oauth2-resource-server:1.0.0")
    testImplementation("io.craigmiller160:testcontainers-common:1.2.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.opencsv:opencsv:5.7.1")
    testImplementation("com.github.javafaker:javafaker:1.0.2") {
        exclude("org.yaml", "snakeyaml")
    }
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.kotest.extensions:kotest-assertions-arrow-jvm:1.2.5")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.postgresql:postgresql:42.6.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("io.github.craigmiller160:spring-fp-result-kt:2.0.0")
    implementation("io.craigmiller160:spring-web-utils:2.0.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    constraints {
        implementation("org.hibernate:hibernate-core:6.2.4.Final") {
            because("Trying to fix inheritance bug with Types")
        }
    }
}

kapt {
    generateStubs = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
        jvmTarget = "19"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configure<SpotlessExtension> {
    kotlin {
        ktfmt("0.43")
    }
    java {
        googleJavaFormat()
    }
}

tasks.withType<BootRun> {
    args = listOf(
        "--spring.profiles.active=dev"
    )
}