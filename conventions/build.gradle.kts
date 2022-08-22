import me.clipi.gradle.defaultCompilationOptions

plugins {
    kotlin("jvm") version "1.5.31"
    id("me.clipi.gradle") version "latest.release"
    `kotlin-dsl`
    `maven-publish`
}

group = "me.clipi.gradle"

gradleUtils {
}
defaultCompilationOptions {
    javaCompatibilityVersion.set(JavaVersion.VERSION_11)
}

publishing {
    repositories {
        mavenLocal()
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.ajoberstar.git-publish:org.ajoberstar.git-publish.gradle.plugin:4.1.1")
    implementation("co.uzzu.dotenv.gradle:co.uzzu.dotenv.gradle.gradle.plugin:2.0.0")
}