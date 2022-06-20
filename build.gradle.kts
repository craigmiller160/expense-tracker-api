import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    val kotlinVersion = "1.6.21"

    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("com.diffplug.spotless") version "6.6.1"
}

group = "io.craigmiller160"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_18
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://craigmiller160.ddns.net:30003/repository/maven-public")
    }
}

val testContainersVersion = "1.17.2"

dependencies {
    val arrowVersion = "1.0.1"
    val kotestArrowVersion = "1.2.5"
    val springArrowKtVersion = "1.0.0-SNAPSHOT"
    val springOAuth2UtilsVersion = "1.10.0-SNAPSHOT"
    val springWebUtilsVersion = "1.1.3"
    val mockitoKotlinVersion = "4.0.0"
    val arrowKtVersion = "1.1.2"
    val assertJVersion = "3.23.1"

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow-jvm:$kotestArrowVersion")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("io.github.craigmiller160:spring-arrow-kt:$springArrowKtVersion")
    implementation("io.craigmiller160:spring-oauth2-utils:$springOAuth2UtilsVersion")
    implementation("io.craigmiller160:spring-web-utils:$springWebUtilsVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testContainersVersion")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    withType<Test> {
        useJUnitPlatform()
        environment("spring.config.location", "classpath:application.yml,classpath:application.test.yml")
    }
}

configure<SpotlessExtension> {
    kotlin {
        ktfmt()
    }
}