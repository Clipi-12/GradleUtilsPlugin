import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktVer = "1.5.31"
plugins {
    kotlin("jvm") version "1.5.31"
    `java-gradle-plugin`
    `maven-publish`
}

group = "me.clipi"
gradlePlugin.plugins.create("ThePlugin") {
    id = "me.clipi.gradle"
    implementationClass = "me.clipi.gradle.plugin.GradleUtilsPlugin"
}

kotlin {
    explicitApi()
}

publishing {
    repositories {
        mavenLocal()
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "Clipi"
        url = uri("https://clipi-repo.herokuapp.com/")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
tasks.withType<AbstractCompile> {
    val version = JavaVersion.VERSION_11.toString()
    sourceCompatibility = version
    targetCompatibility = version
    if (this is KotlinCompile) {
        kotlinOptions.jvmTarget = version
    }
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8", ktVer))
    implementation(kotlin("reflect", ktVer))
    implementation(gradleKotlinDsl())
    implementation("me.clipi.gradle:conventions:$version")
    implementation("me.clipi:bc-manipulator:latest.release")
}
