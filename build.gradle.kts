val version: String by project

setVersion(version)

buildscript {
    dependencies {
        repositories {
            mavenCentral()
            maven {
                setUrl("https://plugins.gradle.org/m2/")
            }
        }
    }
}

plugins {
    java
    kotlin("jvm") version "1.3.11"
    maven
    idea
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "1.3.11")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.3.11")
    implementation(group = "de.undercouch", name = "gradle-download-task", version = "3.4.3")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.8.1")
    implementation(group = "com.google.guava", name = "guava", version = "26.0-jre")
    implementation(group = "com.squareup", name = "javapoet", version = "1.11.1")

    api(gradleApi())

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.3.2")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = "5.3.2")
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.11.1")

    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.3.2")
}

group = "ch.leadrian.samp.kamp"

tasks {
    compileKotlin {
        sourceCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    compileTestKotlin {
        sourceCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    test {
        useJUnitPlatform()
    }
}
